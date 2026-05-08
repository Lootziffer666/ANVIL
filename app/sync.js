/* ═══════════════════════════════════════════
   Anvil — Workspace Sync Protocol (Gate A19)
   Export · Import · Merge · Cross-Device
   ═══════════════════════════════════════════ */

const AnvilSync = (function () {
  "use strict";

  const SYNC_VERSION = 1;
  const SYNC_KEY = "anvil_sync_manifest";

  // ── Export Workspace Bundle ────────────
  function exportBundle() {
    var bundle = {
      _anvil_sync: true,
      version: SYNC_VERSION,
      exported_at: new Date().toISOString(),
      exported_from: AnvilPlatform.current,
      device_id: getDeviceId(),

      // Workspace state
      workspace: null,
      modules: {},
      providers: {},
      tokens_meta: [],  // NEVER export actual keys
      custom_providers: JSON.parse(localStorage.getItem("anvil_custom_providers") || "[]"),
      settings: {
        theme: AnvilPlatform.loadLocal("theme"),
        last_project: AnvilPlatform.loadLocal("last_project"),
        platform_prefs: AnvilPlatform.loadLocal("platform_prefs")
      },
      outputs_manifest: AnvilPlatform.loadLocal("outputs_registry") || []
    };

    // Export token metadata (labels, providers) — never the keys
    var tokens = JSON.parse(localStorage.getItem("anvil_tokens") || "[]");
    bundle.tokens_meta = tokens.map(function (t) {
      return {
        token_id: t.token_id,
        provider: t.provider,
        label: t.label,
        created_at: t.created_at,
        // key is NEVER exported
        needs_reentry: true
      };
    });

    return bundle;
  }

  // ── Import Workspace Bundle ────────────
  function importBundle(bundleJson) {
    var bundle;
    if (typeof bundleJson === "string") {
      bundle = JSON.parse(bundleJson);
    } else {
      bundle = bundleJson;
    }

    if (!bundle._anvil_sync) throw new Error("Kein gültiges Anvil Sync Bundle.");
    if (bundle.version > SYNC_VERSION) throw new Error("Bundle-Version zu neu. Anvil updaten.");

    var report = {
      imported_at: new Date().toISOString(),
      from_platform: bundle.exported_from,
      from_device: bundle.device_id,
      custom_providers_added: 0,
      tokens_needing_reentry: 0,
      settings_restored: 0
    };

    // Restore custom providers
    if (bundle.custom_providers && bundle.custom_providers.length) {
      bundle.custom_providers.forEach(function (p) {
        if (!PROVIDER_REGISTRY[p.id]) {
          try { addCustomProvider(p.id, p.name, p.apiBase, p.authType); report.custom_providers_added++; }
          catch (_) {}
        }
      });
    }

    // Flag tokens that need re-entry on this device
    if (bundle.tokens_meta && bundle.tokens_meta.length) {
      report.tokens_needing_reentry = bundle.tokens_meta.length;
    }

    // Restore settings
    if (bundle.settings) {
      Object.keys(bundle.settings).forEach(function (k) {
        if (bundle.settings[k] != null) {
          AnvilPlatform.saveLocal(k, bundle.settings[k]);
          report.settings_restored++;
        }
      });
    }

    // Save sync manifest
    var manifest = loadSyncManifest();
    manifest.last_import = report;
    manifest.sync_history.push({
      type: "import",
      at: report.imported_at,
      from: bundle.exported_from
    });
    saveSyncManifest(manifest);

    return report;
  }

  // ── Device Identity ────────────────────
  function getDeviceId() {
    var id = localStorage.getItem("anvil_device_id");
    if (!id) {
      id = "ANVIL_" + AnvilPlatform.current.toUpperCase() + "_" +
           Date.now().toString(36) + "_" +
           Math.random().toString(36).slice(2, 8);
      localStorage.setItem("anvil_device_id", id);
    }
    return id;
  }

  // ── Sync Manifest ─────────────────────
  function loadSyncManifest() {
    return AnvilPlatform.loadLocal("sync_manifest") || {
      device_id: getDeviceId(),
      platform: AnvilPlatform.current,
      last_export: null,
      last_import: null,
      sync_history: []
    };
  }

  function saveSyncManifest(manifest) {
    AnvilPlatform.saveLocal("sync_manifest", manifest);
  }

  // ── Git-Based Sync (Reference) ─────────
  // For users with Gitea or GitHub:
  // 1. Export bundle → save as .anvil-sync.json
  // 2. Commit + push
  // 3. On other device: pull + import
  function getGitSyncInstructions() {
    return [
      "1. Exportiere Bundle: AnvilSync.exportBundle()",
      "2. Speichere als .anvil-sync.json im Repo",
      "3. git add .anvil-sync.json && git commit -m 'sync' && git push",
      "4. Auf anderem Gerät: git pull && Anvil → Import",
      "",
      "Empfohlen: go-gitea/gitea für Self-Hosted Git"
    ].join("\n");
  }

  // ── Download Bundle as File ────────────
  function downloadBundle() {
    var bundle = exportBundle();
    var json = JSON.stringify(bundle, null, 2);
    var blob = new Blob([json], { type: "application/json" });
    var url = URL.createObjectURL(blob);
    var a = document.createElement("a");
    a.href = url;
    a.download = "anvil-sync-" + AnvilPlatform.current + "-" +
                 new Date().toISOString().slice(0, 10) + ".json";
    a.click();
    URL.revokeObjectURL(url);

    var manifest = loadSyncManifest();
    manifest.last_export = {
      exported_at: new Date().toISOString(),
      platform: AnvilPlatform.current
    };
    manifest.sync_history.push({
      type: "export",
      at: new Date().toISOString(),
      to: "file"
    });
    saveSyncManifest(manifest);

    return bundle;
  }

  return {
    exportBundle: exportBundle,
    importBundle: importBundle,
    downloadBundle: downloadBundle,
    getDeviceId: getDeviceId,
    loadSyncManifest: loadSyncManifest,
    getGitSyncInstructions: getGitSyncInstructions,
    SYNC_VERSION: SYNC_VERSION
  };
})();
