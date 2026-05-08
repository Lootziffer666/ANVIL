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


/**
 * Provider Registry (Gate A14)
 */
const PROVIDER_REGISTRY = {
  "nvidia": {
    name: "Nvidia NIM",
    icon: "🟢",
    apiBase: "https://integrate.api.nvidia.com/v1",
    authType: "bearer",
    authHeader: "Authorization",
    status: "needs-token",
    builtin: true,
    models: []  // populated in Gate A15
  },
  "huggingface": {
    name: "HuggingFace",
    icon: "🤗",
    apiBase: "https://api-inference.huggingface.co",
    authType: "bearer",
    authHeader: "Authorization",
    status: "needs-token",
    builtin: true,
    models: []  // populated in Gate A16
  },
  "openai": {
    name: "OpenAI",
    icon: "🧠",
    apiBase: "https://api.openai.com/v1",
    authType: "bearer",
    authHeader: "Authorization",
    status: "needs-token",
    builtin: true,
    models: [
      { id: "gpt-4.1", name: "GPT-4.1", type: "chat", context: 1047576 },
      { id: "gpt-4.1-mini", name: "GPT-4.1 Mini", type: "chat", context: 1047576 },
      { id: "gpt-4.1-nano", name: "GPT-4.1 Nano", type: "chat", context: 1047576 },
      { id: "o3", name: "o3", type: "reasoning", context: 200000 },
      { id: "o4-mini", name: "o4-mini", type: "reasoning", context: 200000 },
      { id: "gpt-4o", name: "GPT-4o", type: "chat", context: 128000 },
      { id: "gpt-4o-mini", name: "GPT-4o Mini", type: "chat", context: 128000 }
    ]
  },
  "anthropic": {
    name: "Anthropic",
    icon: "🔶",
    apiBase: "https://api.anthropic.com/v1",
    authType: "api-key",
    authHeader: "x-api-key",
    status: "needs-token",
    builtin: true,
    models: [
      { id: "claude-sonnet-4-20250514", name: "Claude Sonnet 4", type: "chat", context: 200000 },
      { id: "claude-opus-4-20250514", name: "Claude Opus 4", type: "chat", context: 200000 },
      { id: "claude-3-5-haiku-20241022", name: "Claude 3.5 Haiku", type: "chat", context: 200000 }
    ]
  },
  "google": {
    name: "Google AI",
    icon: "🔵",
    apiBase: "https://generativelanguage.googleapis.com/v1beta",
    authType: "query-param",
    authHeader: "key",
    status: "needs-token",
    builtin: true,
    models: [
      { id: "gemini-2.5-pro", name: "Gemini 2.5 Pro", type: "chat", context: 1048576 },
      { id: "gemini-2.5-flash", name: "Gemini 2.5 Flash", type: "chat", context: 1048576 },
      { id: "gemini-2.0-flash", name: "Gemini 2.0 Flash", type: "chat", context: 1048576 }
    ]
  },
  "groq": {
    name: "Groq",
    icon: "⚡",
    apiBase: "https://api.groq.com/openai/v1",
    authType: "bearer",
    authHeader: "Authorization",
    status: "needs-token",
    builtin: true,
    models: [
      { id: "llama-3.3-70b-versatile", name: "Llama 3.3 70B", type: "chat", context: 128000 },
      { id: "meta-llama/llama-4-scout-17b-16e-instruct", name: "Llama 4 Scout", type: "chat", context: 512000 },
      { id: "meta-llama/llama-4-maverick-17b-128e-instruct", name: "Llama 4 Maverick", type: "chat", context: 128000 },
      { id: "deepseek-r1-distill-llama-70b", name: "DeepSeek R1 70B", type: "reasoning", context: 128000 }
    ]
  },
  "mistral": {
    name: "Mistral AI",
    icon: "🌀",
    apiBase: "https://api.mistral.ai/v1",
    authType: "bearer",
    authHeader: "Authorization",
    status: "needs-token",
    builtin: true,
    models: [
      { id: "mistral-large-latest", name: "Mistral Large", type: "chat", context: 128000 },
      { id: "mistral-medium-latest", name: "Mistral Medium", type: "chat", context: 128000 },
      { id: "codestral-latest", name: "Codestral", type: "code", context: 256000 }
    ]
  },
  "together": {
    name: "Together AI",
    icon: "🤝",
    apiBase: "https://api.together.xyz/v1",
    authType: "bearer",
    authHeader: "Authorization",
    status: "needs-token",
    builtin: true,
    models: [
      { id: "meta-llama/Llama-3.3-70B-Instruct-Turbo", name: "Llama 3.3 70B Turbo", type: "chat", context: 128000 },
      { id: "deepseek-ai/DeepSeek-R1", name: "DeepSeek R1", type: "reasoning", context: 64000 },
      { id: "Qwen/Qwen2.5-Coder-32B-Instruct", name: "Qwen 2.5 Coder 32B", type: "code", context: 32000 }
    ]
  },
  "ollama": {
    name: "Ollama (Lokal)",
    icon: "🏠",
    apiBase: "http://localhost:11434",
    authType: "none",
    authHeader: null,
    status: "offline",
    builtin: true,
    models: []
  }
};

/**
 * Add a custom provider
 */
function addCustomProvider(id, name, apiBase, authType) {
  if (PROVIDER_REGISTRY[id]) throw new Error("Provider " + id + " existiert bereits.");
  PROVIDER_REGISTRY[id] = {
    name: name, icon: "🔌", apiBase: apiBase, authType: authType || "bearer",
    authHeader: "Authorization", status: "needs-token", builtin: false, models: []
  };
  // Persist custom providers
  var custom = JSON.parse(localStorage.getItem("anvil_custom_providers") || "[]");
  custom.push({ id: id, name: name, apiBase: apiBase, authType: authType || "bearer" });
  localStorage.setItem("anvil_custom_providers", JSON.stringify(custom));
  return PROVIDER_REGISTRY[id];
}

/**
 * Load custom providers from storage
 */
function loadCustomProviders() {
  var custom = JSON.parse(localStorage.getItem("anvil_custom_providers") || "[]");
  custom.forEach(function (p) {
    if (!PROVIDER_REGISTRY[p.id]) {
      PROVIDER_REGISTRY[p.id] = {
        name: p.name, icon: "🔌", apiBase: p.apiBase, authType: p.authType,
        authHeader: "Authorization", status: "needs-token", builtin: false, models: []
      };
    }
  });
}
