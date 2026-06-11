export interface ProviderSetup {
  id: string
  name: string
  baseUrl: string
  apiKeyEnvVar: string
  models: string[]
  local: boolean
  headers: Record<string, string>
  signupUrl?: string
  keyHint?: string
  category: 'preferred' | 'cloud' | 'local'
}

export const KNOWN_PROVIDERS: ProviderSetup[] = [
  // ── Bevorzugt ────────────────────────────────────────────────────────────────
  {
    id: 'openrouter', name: 'OpenRouter', category: 'preferred',
    baseUrl: 'https://openrouter.ai/api/v1', apiKeyEnvVar: 'OPENROUTER_API_KEY',
    local: false, models: [], signupUrl: 'https://openrouter.ai/keys',
    headers: { 'HTTP-Referer': 'https://anvil.local', 'X-Title': 'Anvil Bellows' },
    keyHint: 'sk-or-...',
  },
  {
    id: 'openai', name: 'OpenAI', category: 'preferred',
    baseUrl: 'https://api.openai.com/v1', apiKeyEnvVar: 'OPENAI_API_KEY',
    local: false, models: [], signupUrl: 'https://platform.openai.com/api-keys',
    headers: {}, keyHint: 'sk-...',
  },
  {
    id: 'anthropic', name: 'Anthropic', category: 'preferred',
    baseUrl: 'https://api.anthropic.com/v1', apiKeyEnvVar: 'ANTHROPIC_API_KEY',
    local: false, models: [], signupUrl: 'https://console.anthropic.com/',
    headers: {}, keyHint: 'sk-ant-...',
  },
  {
    id: 'google-gemini', name: 'Google Gemini', category: 'preferred',
    baseUrl: 'https://generativelanguage.googleapis.com/v1beta/openai', apiKeyEnvVar: 'GEMINI_API_KEY',
    local: false, models: [], signupUrl: 'https://aistudio.google.com/app/apikey',
    headers: {}, keyHint: 'AIza...',
  },

  // ── Cloud ─────────────────────────────────────────────────────────────────────
  {
    id: 'groq', name: 'Groq', category: 'cloud',
    baseUrl: 'https://api.groq.com/openai/v1', apiKeyEnvVar: 'GROQ_API_KEY',
    local: false, models: [], signupUrl: 'https://console.groq.com/keys',
    headers: {}, keyHint: 'gsk_...',
  },
  {
    id: 'together', name: 'Together AI', category: 'cloud',
    baseUrl: 'https://api.together.xyz/v1', apiKeyEnvVar: 'TOGETHER_API_KEY',
    local: false, models: [], signupUrl: 'https://api.together.ai/settings/api-keys',
    headers: {},
  },
  {
    id: 'mistral', name: 'Mistral AI', category: 'cloud',
    baseUrl: 'https://api.mistral.ai/v1', apiKeyEnvVar: 'MISTRAL_API_KEY',
    local: false, models: [], signupUrl: 'https://console.mistral.ai/api-keys/',
    headers: {},
  },
  {
    id: 'cohere', name: 'Cohere', category: 'cloud',
    baseUrl: 'https://api.cohere.com/compatibility/v1', apiKeyEnvVar: 'COHERE_API_KEY',
    local: false, models: [], signupUrl: 'https://dashboard.cohere.com/api-keys',
    headers: {},
  },
  {
    id: 'deepseek', name: 'DeepSeek', category: 'cloud',
    baseUrl: 'https://api.deepseek.com/v1', apiKeyEnvVar: 'DEEPSEEK_API_KEY',
    local: false, models: [], signupUrl: 'https://platform.deepseek.com/api_keys',
    headers: {},
  },
  {
    id: 'xai', name: 'xAI (Grok)', category: 'cloud',
    baseUrl: 'https://api.x.ai/v1', apiKeyEnvVar: 'XAI_API_KEY',
    local: false, models: [], signupUrl: 'https://console.x.ai/',
    headers: {}, keyHint: 'xai-...',
  },
  {
    id: 'perplexity', name: 'Perplexity', category: 'cloud',
    baseUrl: 'https://api.perplexity.ai', apiKeyEnvVar: 'PERPLEXITY_API_KEY',
    local: false, models: [], signupUrl: 'https://www.perplexity.ai/settings/api',
    headers: {}, keyHint: 'pplx-...',
  },
  {
    id: 'nvidia-nim', name: 'NVIDIA NIM', category: 'cloud',
    baseUrl: 'https://integrate.api.nvidia.com/v1', apiKeyEnvVar: 'NVIDIA_API_KEY',
    local: false, models: [], signupUrl: 'https://build.nvidia.com/explore/discover',
    headers: {}, keyHint: 'nvapi-...',
  },
  {
    id: 'fireworks', name: 'Fireworks AI', category: 'cloud',
    baseUrl: 'https://api.fireworks.ai/inference/v1', apiKeyEnvVar: 'FIREWORKS_API_KEY',
    local: false, models: [], signupUrl: 'https://fireworks.ai/account/api-keys',
    headers: {}, keyHint: 'fw_...',
  },
  {
    id: 'cerebras', name: 'Cerebras', category: 'cloud',
    baseUrl: 'https://api.cerebras.ai/v1', apiKeyEnvVar: 'CEREBRAS_API_KEY',
    local: false, models: [], signupUrl: 'https://cloud.cerebras.ai/',
    headers: {}, keyHint: 'csk-...',
  },
  {
    id: 'sambanova', name: 'SambaNova', category: 'cloud',
    baseUrl: 'https://api.sambanova.ai/v1', apiKeyEnvVar: 'SAMBANOVA_API_KEY',
    local: false, models: [], signupUrl: 'https://cloud.sambanova.ai/apis',
    headers: {},
  },
  {
    id: 'deepinfra', name: 'DeepInfra', category: 'cloud',
    baseUrl: 'https://api.deepinfra.com/v1/openai', apiKeyEnvVar: 'DEEPINFRA_API_KEY',
    local: false, models: [], signupUrl: 'https://deepinfra.com/dash/api_keys',
    headers: {},
  },
  {
    id: 'huggingface', name: 'HuggingFace', category: 'cloud',
    baseUrl: 'https://api-inference.huggingface.co/v1', apiKeyEnvVar: 'HF_API_KEY',
    local: false, models: [], signupUrl: 'https://huggingface.co/settings/tokens',
    headers: {}, keyHint: 'hf_...',
  },
  {
    id: 'ai21', name: 'AI21 Labs', category: 'cloud',
    baseUrl: 'https://api.ai21.com/studio/v1', apiKeyEnvVar: 'AI21_API_KEY',
    local: false, models: [], signupUrl: 'https://studio.ai21.com/account/api-key',
    headers: {},
  },
  {
    id: 'novita', name: 'Novita AI', category: 'cloud',
    baseUrl: 'https://api.novita.ai/v1/openai', apiKeyEnvVar: 'NOVITA_API_KEY',
    local: false, models: [], signupUrl: 'https://novita.ai/dashboard/key',
    headers: {},
  },
  {
    id: 'lepton', name: 'Lepton AI', category: 'cloud',
    baseUrl: 'https://api.lepton.ai/api/v1', apiKeyEnvVar: 'LEPTON_API_KEY',
    local: false, models: [], signupUrl: 'https://dashboard.lepton.ai/',
    headers: {},
  },
  {
    id: 'replicate', name: 'Replicate', category: 'cloud',
    baseUrl: 'https://openai-compat.replicate.com/v1', apiKeyEnvVar: 'REPLICATE_API_KEY',
    local: false, models: [], signupUrl: 'https://replicate.com/account/api-tokens',
    headers: {}, keyHint: 'r8_...',
  },
  {
    id: 'anyscale', name: 'Anyscale', category: 'cloud',
    baseUrl: 'https://api.endpoints.anyscale.com/v1', apiKeyEnvVar: 'ANYSCALE_API_KEY',
    local: false, models: [], signupUrl: 'https://app.endpoints.anyscale.com/credentials',
    headers: {}, keyHint: 'esecret_...',
  },
  {
    id: 'cloudflare', name: 'Cloudflare Workers AI', category: 'cloud',
    baseUrl: 'https://api.cloudflare.com/client/v4/accounts/ACCOUNT_ID/ai/v1', apiKeyEnvVar: 'CLOUDFLARE_API_KEY',
    local: false, models: [], signupUrl: 'https://dash.cloudflare.com/profile/api-tokens',
    headers: {},
  },

  // ── Lokal ─────────────────────────────────────────────────────────────────────
  {
    id: 'ollama', name: 'Ollama', category: 'local',
    baseUrl: 'http://localhost:11434/v1', apiKeyEnvVar: 'OLLAMA_API_KEY',
    local: true, models: [], headers: {},
  },
  {
    id: 'lmstudio', name: 'LM Studio', category: 'local',
    baseUrl: 'http://localhost:1234/v1', apiKeyEnvVar: 'LMSTUDIO_API_KEY',
    local: true, models: [], headers: {},
  },
  {
    id: 'llamacpp', name: 'llama.cpp', category: 'local',
    baseUrl: 'http://localhost:8080/v1', apiKeyEnvVar: 'LLAMACPP_API_KEY',
    local: true, models: [], headers: {},
  },
  {
    id: 'vllm', name: 'vLLM', category: 'local',
    baseUrl: 'http://localhost:8000/v1', apiKeyEnvVar: 'VLLM_API_KEY',
    local: true, models: [], headers: {},
  },
  {
    id: 'textgen', name: 'Text Gen WebUI', category: 'local',
    baseUrl: 'http://localhost:5000/v1', apiKeyEnvVar: 'TEXTGEN_API_KEY',
    local: true, models: [], headers: {},
  },
  {
    id: 'jan', name: 'Jan AI', category: 'local',
    baseUrl: 'http://localhost:1337/v1', apiKeyEnvVar: 'JAN_API_KEY',
    local: true, models: [], headers: {},
  },
]

export function findKnownProvider(id: string): ProviderSetup | undefined {
  return KNOWN_PROVIDERS.find(p => p.id === id)
}

export function makeCustomProvider(id: string): ProviderSetup {
  return {
    id,
    name: id,
    baseUrl: '',
    apiKeyEnvVar: id.toUpperCase().replace(/[^A-Z0-9]/g, '_') + '_API_KEY',
    models: [],
    local: false,
    headers: {},
    category: 'cloud',
  }
}
