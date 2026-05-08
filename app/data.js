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
  "localai": {
    name: "LocalAI (Any Hardware)",
    icon: "🖥️",
    apiBase: "http://localhost:8080",
    authType: "none",
    authHeader: null,
    status: "offline",
    builtin: true,
    models: [],
    note: "mudler/LocalAI — Run any model on any hardware. No GPU required. Ideal für Nachtläufe."
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


/**
 * Nvidia Build Models — Vollständig (Gate A15)
 * Stand: Mai 2026 — build.nvidia.com
 */
const NVIDIA_MODELS = [
  // Foundation / Chat
  { id: "meta/llama-3.3-70b-instruct", name: "Llama 3.3 70B", type: "chat", context: 128000, params: "70B" },
  { id: "meta/llama-3.1-405b-instruct", name: "Llama 3.1 405B", type: "chat", context: 128000, params: "405B" },
  { id: "meta/llama-3.1-70b-instruct", name: "Llama 3.1 70B", type: "chat", context: 128000, params: "70B" },
  { id: "meta/llama-3.1-8b-instruct", name: "Llama 3.1 8B", type: "chat", context: 128000, params: "8B" },
  { id: "meta/llama-4-scout-17b-16e-instruct", name: "Llama 4 Scout", type: "chat", context: 512000, params: "17B" },
  { id: "meta/llama-4-maverick-17b-128e-instruct", name: "Llama 4 Maverick", type: "chat", context: 256000, params: "17B" },
  { id: "nvidia/llama-3.1-nemotron-70b-instruct", name: "Nemotron 70B", type: "chat", context: 128000, params: "70B" },
  { id: "nvidia/llama-3.3-nemotron-super-49b-v1", name: "Nemotron Super 49B", type: "chat", context: 128000, params: "49B" },
  { id: "nvidia/llama-3.1-nemotron-ultra-253b-v1", name: "Nemotron Ultra 253B", type: "chat", context: 128000, params: "253B" },
  { id: "mistralai/mistral-large-2-instruct", name: "Mistral Large 2", type: "chat", context: 128000, params: "123B" },
  { id: "mistralai/mixtral-8x22b-instruct-v0.1", name: "Mixtral 8x22B", type: "chat", context: 65536, params: "141B" },
  { id: "google/gemma-2-27b-it", name: "Gemma 2 27B", type: "chat", context: 8192, params: "27B" },
  { id: "qwen/qwen2.5-72b-instruct", name: "Qwen 2.5 72B", type: "chat", context: 128000, params: "72B" },
  { id: "deepseek-ai/deepseek-r1", name: "DeepSeek R1", type: "reasoning", context: 65536, params: "671B" },

  // Code
  { id: "nvidia/llama-3.1-nemotron-nano-8b-v1", name: "Nemotron Nano 8B", type: "code", context: 128000, params: "8B" },
  { id: "qwen/qwen2.5-coder-32b-instruct", name: "Qwen 2.5 Coder 32B", type: "code", context: 32768, params: "32B" },
  { id: "meta/codellama-70b", name: "Code Llama 70B", type: "code", context: 16384, params: "70B" },

  // Vision
  { id: "nvidia/vila", name: "VILA", type: "vision", context: 4096, params: null },
  { id: "microsoft/phi-3.5-vision-instruct", name: "Phi 3.5 Vision", type: "vision", context: 128000, params: "4B" },
  { id: "meta/llama-3.2-90b-vision-instruct", name: "Llama 3.2 90B Vision", type: "vision", context: 128000, params: "90B" },
  { id: "meta/llama-3.2-11b-vision-instruct", name: "Llama 3.2 11B Vision", type: "vision", context: 128000, params: "11B" },
  { id: "google/deplot", name: "DePlot", type: "vision", context: 2048, params: null },

  // Embedding
  { id: "nvidia/nv-embedqa-e5-v5", name: "NV-EmbedQA E5 v5", type: "embedding", dims: 1024 },
  { id: "nvidia/nv-embed-v2", name: "NV-Embed v2", type: "embedding", dims: 4096 },
  { id: "snowflake/arctic-embed-l-v2.0", name: "Arctic Embed L v2", type: "embedding", dims: 1024 },
  { id: "baai/bge-m3", name: "BGE-M3", type: "embedding", dims: 1024 },

  // Reranking
  { id: "nvidia/nv-rerankqa-mistral-4b-v3", name: "NV-RerankQA Mistral 4B", type: "reranking" },
  { id: "nvidia/llama-3.2-nv-rerankqa-1b-v2", name: "Llama 3.2 NV-RerankQA 1B", type: "reranking" },

  // Image Generation
  { id: "nvidia/consistory", name: "Consistory", type: "image-gen" },
  { id: "stabilityai/stable-diffusion-3-5-large", name: "Stable Diffusion 3.5 Large", type: "image-gen" },
  { id: "black-forest-labs/flux-schnell", name: "FLUX Schnell", type: "image-gen" },

  // Speech
  { id: "nvidia/parakeet-ctc-0.6b-asr", name: "Parakeet ASR 0.6B", type: "speech-to-text" },
  { id: "nvidia/fastpitch-hifigan-tts", name: "FastPitch HiFiGAN", type: "text-to-speech" }
];

// Wire into Provider Registry
if (typeof PROVIDER_REGISTRY !== "undefined" && PROVIDER_REGISTRY.nvidia) {
  PROVIDER_REGISTRY.nvidia.models = NVIDIA_MODELS;
}


/**
 * HuggingFace Top Models (Gate A16)
 */
const HF_TOP_MODELS = [
  // Text Generation
  { id: "meta-llama/Llama-3.3-70B-Instruct", name: "Llama 3.3 70B Instruct", task: "text-generation", downloads: "10M+", license: "llama3.3" },
  { id: "meta-llama/Llama-4-Scout-17B-16E-Instruct", name: "Llama 4 Scout", task: "text-generation", downloads: "2M+", license: "llama4" },
  { id: "meta-llama/Llama-4-Maverick-17B-128E-Instruct", name: "Llama 4 Maverick", task: "text-generation", downloads: "1M+", license: "llama4" },
  { id: "mistralai/Mistral-Large-Instruct-2411", name: "Mistral Large", task: "text-generation", downloads: "1M+", license: "apache-2.0" },
  { id: "Qwen/Qwen2.5-72B-Instruct", name: "Qwen 2.5 72B", task: "text-generation", downloads: "5M+", license: "qwen" },
  { id: "Qwen/Qwen2.5-Coder-32B-Instruct", name: "Qwen 2.5 Coder 32B", task: "text-generation", downloads: "3M+", license: "qwen" },
  { id: "google/gemma-2-27b-it", name: "Gemma 2 27B", task: "text-generation", downloads: "2M+", license: "gemma" },
  { id: "deepseek-ai/DeepSeek-R1", name: "DeepSeek R1", task: "text-generation", downloads: "5M+", license: "mit" },
  { id: "microsoft/phi-3.5-mini-instruct", name: "Phi 3.5 Mini", task: "text-generation", downloads: "3M+", license: "mit" },
  { id: "nvidia/Llama-3.1-Nemotron-70B-Instruct-HF", name: "Nemotron 70B", task: "text-generation", downloads: "500K+", license: "llama3.1" },

  // Embeddings
  { id: "sentence-transformers/all-MiniLM-L6-v2", name: "MiniLM L6 v2", task: "sentence-similarity", downloads: "50M+", license: "apache-2.0" },
  { id: "BAAI/bge-large-en-v1.5", name: "BGE Large EN", task: "sentence-similarity", downloads: "10M+", license: "mit" },

  // Speech
  { id: "openai/whisper-large-v3", name: "Whisper Large v3", task: "automatic-speech-recognition", downloads: "10M+", license: "apache-2.0" },
  { id: "openai/whisper-large-v3-turbo", name: "Whisper Large v3 Turbo", task: "automatic-speech-recognition", downloads: "5M+", license: "apache-2.0" },

  // Image
  { id: "stabilityai/stable-diffusion-xl-base-1.0", name: "SDXL Base", task: "text-to-image", downloads: "15M+", license: "openrail++" },
  { id: "black-forest-labs/FLUX.1-schnell", name: "FLUX.1 Schnell", task: "text-to-image", downloads: "3M+", license: "apache-2.0" },

  // Classification
  { id: "facebook/bart-large-mnli", name: "BART Large MNLI", task: "zero-shot-classification", downloads: "10M+", license: "apache-2.0" },
  { id: "cross-encoder/ms-marco-MiniLM-L-12-v2", name: "MS MARCO MiniLM", task: "text-ranking", downloads: "5M+", license: "apache-2.0" }
];

const HF_TASK_ICONS = {
  "text-generation": "💬",
  "sentence-similarity": "📊",
  "automatic-speech-recognition": "🎙️",
  "text-to-image": "🎨",
  "zero-shot-classification": "🏷️",
  "text-ranking": "🔀"
};

// Wire into Provider Registry
if (typeof PROVIDER_REGISTRY !== "undefined" && PROVIDER_REGISTRY.huggingface) {
  PROVIDER_REGISTRY.huggingface.models = HF_TOP_MODELS.map(function (m) {
    return { id: m.id, name: m.name, type: m.task, context: null };
  });
}

/**
 * Generate local run commands for a HF model
 */
function getLocalRunCommands(modelId) {
  return {
    ollama: "ollama run " + modelId.split("/").pop().toLowerCase(),
    llamacpp: "llama-cli -m " + modelId.split("/").pop() + ".gguf -p \"Hello\"",
    hfUrl: "https://huggingface.co/" + modelId
  };
}
