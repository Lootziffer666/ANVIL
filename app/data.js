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
