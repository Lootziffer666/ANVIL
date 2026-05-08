/* ═══════════════════════════════════════════
   Anvil — Werkbank App Logic
   State Surface Design · Anti-Dashboard
   ═══════════════════════════════════════════ */

(function () {
  "use strict";

  // ── State ──────────────────────────────
  let currentWorkspace = null;

  // ── DOM refs ───────────────────────────
  const $ = (sel) => document.querySelector(sel);

  const wsStatus       = $("#ws-status");
  const wsBody         = $("#ws-body");
  const wsEmpty        = wsBody.querySelector(".empty-state");
  const wsLoaded       = $("#ws-loaded");
  const wsName         = $("#ws-name");
  const wsDesc         = $("#ws-desc");
  const wsTarget       = $("#ws-target");
  const wsLoadedStatus = $("#ws-loaded-status");

  const modCount  = $("#mod-count");
  const modEmpty  = $("#mod-empty");
  const modList   = $("#mod-list");

  const outEmpty  = $("#out-empty");
  const outList   = $("#out-list");

  const timeline  = $("#status-timeline");
  const statusTime = $("#status-time");

  const forgeOverlay = $("#forge-overlay");
  const forgeBody    = $("#forge-body");

  // ── Init ───────────────────────────────
  function init() {
    statusTime.textContent = timeNow();

    $("#btn-load-ws").addEventListener("click", loadExampleWorkspace);
    $("#btn-forge").addEventListener("click", openForge);
    $("#btn-close-forge").addEventListener("click", closeForge);

    forgeOverlay.addEventListener("click", (e) => {
      if (e.target === forgeOverlay) closeForge();
    });

    document.addEventListener("keydown", (e) => {
      if (e.key === "Escape") closeForge();
    });

    renderForge();
  }

  // ── Workspace ──────────────────────────
  function loadExampleWorkspace() {
    currentWorkspace = { ...EXAMPLE_WORKSPACE };
    renderWorkspace();
    renderModules();
    addStatus("stable", `Workspace „${currentWorkspace.name}" geladen.`);
  }

  function renderWorkspace() {
    if (!currentWorkspace) return;

    wsEmpty.classList.add("hidden");
    wsLoaded.classList.remove("hidden");

    wsName.textContent = currentWorkspace.name;
    wsDesc.textContent = currentWorkspace.description;
    wsTarget.textContent = currentWorkspace.buildTarget;

    setStatusBadge(wsStatus, currentWorkspace.status);
    setStatusBadge(wsLoadedStatus, currentWorkspace.status);
  }

  // ── Modules ────────────────────────────
  function renderModules() {
    if (!currentWorkspace || currentWorkspace.modules.length === 0) {
      modEmpty.classList.remove("hidden");
      modList.classList.add("hidden");
      modCount.textContent = "0 aktiv";
      return;
    }

    modEmpty.classList.add("hidden");
    modList.classList.remove("hidden");
    modList.innerHTML = "";

    currentWorkspace.modules.forEach((modId) => {
      const mod = MODULE_REGISTRY[modId];
      if (!mod) return;

      const li = document.createElement("li");
      li.className = "module-item";
      li.innerHTML = `
        <div>
          <div class="module-name">${mod.name}</div>
          <div class="module-purpose">${mod.purpose}</div>
        </div>
        <span class="status-badge" data-state="${mod.status}">${STATE_LABELS[mod.status]}</span>
      `;
      modList.appendChild(li);
    });

    modCount.textContent = `${currentWorkspace.modules.length} aktiv`;
    addStatus("adapting", `${currentWorkspace.modules.length} Module eingebunden.`);
  }

  // ── Artifacts ──────────────────────────
  function renderArtifacts(artifacts) {
    if (!artifacts || artifacts.length === 0) return;

    outEmpty.classList.add("hidden");
    outList.classList.remove("hidden");
    outList.innerHTML = "";

    artifacts.forEach((art) => {
      const li = document.createElement("li");
      li.className = "artifact-item";
      li.innerHTML = `
        <span class="artifact-name">${art.name}</span>
        <span class="artifact-type">${art.type}</span>
      `;
      outList.appendChild(li);
    });
  }

  // ── The Forge ──────────────────────────
  function renderForge() {
    forgeBody.innerHTML = "";

    Object.entries(MODULE_REGISTRY).forEach(([id, mod]) => {
      const div = document.createElement("div");
      div.className = "forge-module";
      div.innerHTML = `
        <div class="forge-module-info">
          <div class="forge-module-name">${mod.name}</div>
          <div class="forge-module-purpose">${mod.purpose}</div>
        </div>
        <div class="forge-module-meta">
          <span class="status-badge forge-module-status" data-state="${mod.status}">${STATE_LABELS[mod.status]}</span>
          <button class="btn-forge-open" data-mod="${id}">Öffnen</button>
        </div>
      `;
      forgeBody.appendChild(div);
    });

    // Open-buttons (no-op for now, but wired)
    forgeBody.querySelectorAll(".btn-forge-open").forEach((btn) => {
      btn.addEventListener("click", () => {
        const modId = btn.dataset.mod;
        addStatus("adapting", `Modul „${MODULE_REGISTRY[modId].name}" ausgewählt.`);
        closeForge();
      });
    });
  }

  function openForge() {
    forgeOverlay.classList.remove("hidden");
  }

  function closeForge() {
    forgeOverlay.classList.add("hidden");
  }

  // ── Status Timeline ────────────────────
  function addStatus(state, text) {
    const entry = document.createElement("div");
    entry.className = "status-entry";
    entry.dataset.state = state;
    entry.innerHTML = `
      <span class="status-dot"></span>
      <span class="status-text">${text}</span>
      <span class="status-time">${timeNow()}</span>
    `;
    timeline.prepend(entry);
  }

  // ── Helpers ────────────────────────────
  function setStatusBadge(el, state) {
    el.dataset.state = state;
    el.textContent = STATE_LABELS[state] || state;
  }

  function timeNow() {
    const d = new Date();
    return d.toLocaleTimeString("de-DE", { hour: "2-digit", minute: "2-digit", second: "2-digit" });
  }

  // ── Boot ───────────────────────────────
  init();

  // ── Preview Surface (Gate A10) ─────────
  const previewStatus  = $("#preview-status");
  const previewEmpty   = $("#preview-empty");
  const previewContent = $("#preview-content");
  const previewRender  = $("#preview-render");
  const previewError   = $("#preview-error");
  const errorMessage   = $("#error-message");
  const errorHint      = $("#error-hint");
  const errorStack     = $("#error-stack");

  function showPreview(content, type) {
    previewEmpty.classList.add("hidden");
    previewContent.classList.remove("hidden");
    previewError.classList.add("hidden");

    try {
      switch (type) {
        case "markdown":
          previewRender.innerHTML = `<div class="preview-md">${escapeHtml(content)}</div>`;
          break;
        case "json":
          const parsed = JSON.parse(content);
          previewRender.innerHTML = `<pre class="preview-json">${escapeHtml(JSON.stringify(parsed, null, 2))}</pre>`;
          break;
        case "html":
          previewRender.innerHTML = `<iframe class="preview-iframe" srcdoc="${escapeAttr(content)}"></iframe>`;
          break;
        default:
          previewRender.innerHTML = `<pre class="preview-text">${escapeHtml(content)}</pre>`;
      }
      setStatusBadge(previewStatus, "stable");
    } catch (err) {
      showPreviewError(err);
    }
  }

  function showPreviewError(err) {
    previewContent.classList.remove("hidden");
    previewRender.innerHTML = "";
    previewError.classList.remove("hidden");

    const recovery = getRecoveryHint(err);
    errorMessage.textContent = recovery.message;
    errorHint.textContent = recovery.hint;
    errorStack.textContent = err.stack || String(err);
    setStatusBadge(previewStatus, "failed");
  }

  function getRecoveryHint(err) {
    if (err instanceof SyntaxError) {
      return {
        message: "JSON ist ungültig.",
        hint: "Prüfe ob alle Klammern geschlossen sind und Kommas stimmen."
      };
    }
    if (err.message && err.message.includes("encoding")) {
      return {
        message: "Encoding-Problem.",
        hint: "Datei als UTF-8 speichern und erneut versuchen."
      };
    }
    return {
      message: "Vorschau konnte nicht erstellt werden.",
      hint: "Dateiformat prüfen oder anderen Preview-Typ wählen."
    };
  }

  function escapeHtml(str) {
    return str.replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;");
  }

  function escapeAttr(str) {
    return str.replace(/&/g, "&amp;").replace(/"/g, "&quot;").replace(/</g, "&lt;").replace(/>/g, "&gt;");
  }


  // ── Prompt Pack Builder (Gate A12) ─────
  const btnBuildPack = $("#btn-build-pack");
  if (btnBuildPack) {
    btnBuildPack.addEventListener("click", function () {
      const project = $("#ppb-project").value.trim();
      const goal = $("#ppb-goal").value.trim();
      const constraints = $("#ppb-constraints").value.split(",").map(s => s.trim()).filter(Boolean);
      const nextGate = $("#ppb-gate").value.trim();
      const agentTarget = $("#ppb-agent").value;
      const contextFiles = $("#ppb-context").value.split(",").map(s => s.trim()).filter(Boolean);

      try {
        const result = buildPromptPack({
          project, goal, constraints, nextGate, agentTarget, contextFiles
        });
        $("#ppb-output").classList.remove("hidden");
        $("#ppb-result").textContent = result.markdown;
        addStatus("stable", `Prompt Pack für "${project}" erstellt.`);
      } catch (err) {
        $("#ppb-output").classList.remove("hidden");
        $("#ppb-result").textContent = "❌ " + err.message;
        addStatus("failed", err.message);
      }
    });
  }


  // ── Token Management (Gate A13) ────────
  const tokenList = $("#token-list");
  const tokenEmpty = $("#token-empty");
  const tokenForm = $("#token-form");
  const btnAddToken = $("#btn-add-token");
  const btnSaveToken = $("#btn-save-token");
  const btnCancelToken = $("#btn-cancel-token");

  function renderTokens() {
    var tokens = TokenManager.list();
    if (!tokens.length) { tokenEmpty.classList.remove("hidden"); return; }
    tokenEmpty.classList.add("hidden");
    var existing = tokenList.querySelectorAll(".token-item");
    existing.forEach(function (e) { e.remove(); });
    tokens.forEach(function (t) {
      var div = document.createElement("div");
      div.className = "token-item";
      div.innerHTML =
        '<div class="token-info">' +
          '<span class="token-label">' + t.label + '</span>' +
          '<span class="token-provider">' + t.provider + '</span>' +
          '<span class="token-preview">' + t.key_preview + '</span>' +
        '</div>' +
        '<button class="btn-small btn-danger" data-tok="' + t.token_id + '">Löschen</button>';
      div.querySelector(".btn-danger").addEventListener("click", function () {
        if (confirm("Token " + t.label + " wirklich löschen?")) {
          TokenManager.remove(t.token_id);
          renderTokens();
          addStatus("adapting", "Token gelöscht: " + t.label);
        }
      });
      tokenList.appendChild(div);
    });
  }

  function populateProviderSelect() {
    var sel = $("#tok-provider");
    sel.innerHTML = "";
    Object.entries(PROVIDER_REGISTRY).forEach(function (entry) {
      var opt = document.createElement("option");
      opt.value = entry[0]; opt.textContent = entry[1].name;
      sel.appendChild(opt);
    });
  }

  if (btnAddToken) {
    btnAddToken.addEventListener("click", function () {
      populateProviderSelect();
      tokenForm.classList.remove("hidden");
    });
  }
  if (btnCancelToken) {
    btnCancelToken.addEventListener("click", function () { tokenForm.classList.add("hidden"); });
  }
  if (btnSaveToken) {
    btnSaveToken.addEventListener("click", function () {
      try {
        var r = TokenManager.create(
          $("#tok-provider").value, $("#tok-label").value.trim(), $("#tok-key").value
        );
        tokenForm.classList.add("hidden");
        $("#tok-label").value = ""; $("#tok-key").value = "";
        renderTokens();
        addStatus("stable", "Token erstellt: " + r.label);
      } catch (err) { addStatus("failed", err.message); }
    });
  }
  renderTokens();


  // ── Provider Registry (Gate A14) ───────
  loadCustomProviders();

  function renderProviders() {
    var list = $("#provider-list");
    list.innerHTML = "";
    Object.entries(PROVIDER_REGISTRY).forEach(function (entry) {
      var id = entry[0], p = entry[1];
      var tokens = TokenManager.getForProvider(id);
      var st = tokens.length > 0 ? "stable" : (p.authType === "none" ? "stable" : "needs-token");
      var modelCount = p.models ? p.models.length : 0;
      var div = document.createElement("div");
      div.className = "provider-item";
      div.dataset.state = st;
      div.innerHTML =
        '<div class="provider-info">' +
          '<span class="provider-icon">' + p.icon + '</span>' +
          '<span class="provider-name">' + p.name + '</span>' +
          '<span class="provider-models">' + modelCount + ' Modelle</span>' +
        '</div>' +
        '<span class="status-badge" data-state="' + (st === "needs-token" ? "act-now" : "stable") + '">' +
          (st === "needs-token" ? "Token fehlt" : "Bereit") +
        '</span>';
      list.appendChild(div);
    });
  }

  var btnAddProvider = $("#btn-add-provider");
  var provForm = $("#provider-form");
  if (btnAddProvider) {
    btnAddProvider.addEventListener("click", function () { provForm.classList.remove("hidden"); });
  }
  if ($("#btn-cancel-provider")) {
    $("#btn-cancel-provider").addEventListener("click", function () { provForm.classList.add("hidden"); });
  }
  if ($("#btn-save-provider")) {
    $("#btn-save-provider").addEventListener("click", function () {
      try {
        addCustomProvider(
          $("#prov-id").value.trim(), $("#prov-name").value.trim(),
          $("#prov-url").value.trim(), $("#prov-auth").value
        );
        provForm.classList.add("hidden");
        renderProviders();
        addStatus("stable", "Provider hinzugefügt: " + $("#prov-name").value.trim());
      } catch (err) { addStatus("failed", err.message); }
    });
  }
  renderProviders();

})();
