/* ═══════════════════════════════════════════
   Anvil — Data Layer
   Workspace Model · Module Contracts · Forge Registry
   ═══════════════════════════════════════════ */

/**
 * Workspace-Datenstruktur (Gate A4)
 * Definiert in docs/WORKSPACE_MODEL.md
 */
const EXAMPLE_WORKSPACE = {
  name: "Android Text Tool",
  description: "Textverarbeitung für Android — Markdown zu PDF, mit Rechtschreibprüfung.",
  modules: ["text-tool", "file-tool"],
  inputs: [
    { name: "Eingabetext", type: "text/markdown" },
    { name: "Quelldatei", type: "file/*" }
  ],
  outputs: [
    { name: "Verarbeiteter Text", type: "text/plain" },
    { name: "Export-PDF", type: "application/pdf" }
  ],
  buildTarget: "android-apk",
  status: "stable"
};

/**
 * Module Contract Registry (Gate A5)
 * Jedes Modul folgt dem MODULE_CONTRACT.md
 */
const MODULE_REGISTRY = {
  "text-tool": {
    name: "Text Tool",
    purpose: "Textverarbeitung: Formatierung, Transformation, Export.",
    inputs: ["text/plain", "text/markdown"],
    outputs: ["text/plain", "text/html", "application/pdf"],
    requiredPermissions: ["filesystem.read"],
    canRunOffline: true,
    canExport: true,
    failureBehavior: "Gibt unveränderten Input zurück.",
    status: "stable",
    lastOutput: null
  },
  "file-tool": {
    name: "File Tool",
    purpose: "Dateiverwaltung: Lesen, Schreiben, Konvertieren.",
    inputs: ["file/*"],
    outputs: ["file/*", "text/plain"],
    requiredPermissions: ["filesystem.read", "filesystem.write"],
    canRunOffline: true,
    canExport: true,
    failureBehavior: "Fehlermeldung mit Dateipfad und Grund.",
    status: "adapting",
    lastOutput: null
  },
  "prompt-pack-builder": {
    name: "Prompt Pack Builder",
    purpose: "Erzeugt strukturierte Prompt-Pakete für Agent-Übergaben.",
    inputs: ["text/plain", "config/json"],
    outputs: ["text/markdown", "application/json"],
    requiredPermissions: ["filesystem.write"],
    canRunOffline: true,
    canExport: true,
    failureBehavior: "Gibt Fehlermeldung mit fehlendem Pflichtfeld zurück.",
    status: "stable",
    lastOutput: null
  },
  "android-blueprint": {
    name: "Android Blueprint Tool",
    purpose: "APK-Scaffolding: Manifest, Gradle, Signatur-Konfiguration.",
    inputs: ["config/json", "config/yaml"],
    outputs: ["build/gradle-project", "build/apk"],
    requiredPermissions: ["filesystem.read", "filesystem.write", "network.build-server"],
    canRunOffline: false,
    canExport: true,
    failureBehavior: "Build-Log mit Fehlerstelle. Kein partieller Output.",
    status: "act-now",
    lastOutput: null
  }
};

/**
 * Zustandssprache
 */
const STATE_LABELS = {
  "stable":   "Stable",
  "adapting":  "Adapting",
  "act-now":   "Act Now",
  "failed":    "Failed",
  "empty":     "Leer"
};


/**
 * Output Types (Gate A7)
 * Definiert in docs/ARTIFACT_OUTPUT_LAYER.md
 */
const OUTPUT_TYPES = {
  "text/markdown":    { ext: ".md",          label: "Markdown",  icon: "📝" },
  "application/json": { ext: ".json",        label: "JSON",      icon: "📋" },
  "application/zip":  { ext: ".zip",         label: "ZIP",       icon: "📦" },
  "text/x-diff":      { ext: ".patch",       label: "Patch",     icon: "🔧" },
  "text/prompt":      { ext: ".prompt.md",   label: "Prompt",    icon: "🤖" },
  "application/config": { ext: ".config.json", label: "Config", icon: "⚙️" },
};

/**
 * Create output manifest
 */
function createOutputManifest(module, workspace, type, filename) {
  const now = new Date();
  const ts = now.toISOString().replace(/[-:T]/g, "").slice(0, 14);
  const id = `OUT_${ts}_${String(Math.floor(Math.random() * 999) + 1).padStart(3, "0")}`;
  return {
    output_id: id,
    created_at: now.toISOString(),
    origin: { module, workspace, run_id: `RUN_${ts}` },
    type,
    filename,
    size_bytes: 0,
    checksum_sha256: null,
  };
}


/**
 * Build Targets (Gate A9)
 * Definiert in docs/ANDROID_BLUEPRINT_TRACK.md
 */
const BUILD_TARGETS = {
  "prompt-pack-builder": {
    name: "Prompt Pack Builder",
    purpose: "Erzeugt strukturierte Prompt-Pakete für Agent-Übergaben.",
    inputs: ["text/plain", "config/json"],
    outputs: ["text/markdown", "application/json"],
    requiredPermissions: ["filesystem.write"],
    canRunOffline: true,
    canExport: true,
    failureBehavior: "Gibt Fehlermeldung mit fehlendem Pflichtfeld zurück.",
    status: "stable",
    lastOutput: null
  },
  "android-blueprint": {
    label: "Android Blueprint",
    icon: "📐",
    description: "Tasker/App-Factory Export — Bauplan, keine fertige App.",
    status: "active",
    warning: "Blueprint-Modus: Erzeugt einen Bauplan, keine fertige App."
  },
  "android-apk": {
    label: "Android APK",
    icon: "📱",
    description: "Nativer APK Build.",
    status: "planned",
    warning: null
  },
  "windows-exe": {
    label: "Windows EXE",
    icon: "🖥️",
    description: "Windows Desktop App.",
    status: "planned",
    warning: null
  },
  "export-only": {
    label: "Nur Export",
    icon: "📤",
    description: "Artefakte exportieren, kein Build.",
    status: "active",
    warning: null
  }
};
