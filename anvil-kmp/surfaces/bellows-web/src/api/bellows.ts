export type QualityState = 'STABLE' | 'DEGRADED' | 'BLOCKED' | 'FAILED'

export interface HealthResponse {
  status: QualityState
  adapters: Record<string, QualityState>
  models: string[]
}

export interface ModelCard {
  id: string
  object: string
  owned_by?: string
}

export interface ModelsResponse {
  object: string
  data: ModelCard[]
}

export interface ChatMessage {
  role: 'user' | 'assistant' | 'system'
  content: string
}

export interface ChatRequest {
  model?: string
  messages: ChatMessage[]
  max_tokens?: number
  temperature?: number
  stream?: boolean
}

export interface ChatResponse {
  id: string
  object: string
  model: string
  choices: Array<{
    index: number
    message: ChatMessage
    finish_reason: string
  }>
  usage?: {
    prompt_tokens: number
    completion_tokens: number
    total_tokens: number
  }
}

function getGatewayUrl(): string {
  return (
    localStorage.getItem('bellows_gateway_url') ||
    (import.meta.env.VITE_BELLOWS_GATEWAY_URL as string | undefined) ||
    'http://localhost:8765'
  )
}

function buildHeaders(includeContentType = false): Record<string, string> {
  const headers: Record<string, string> = {}
  if (includeContentType) headers['Content-Type'] = 'application/json'
  const key = sessionStorage.getItem('bellows_gateway_key') || ''
  if (key) headers['Authorization'] = `Bearer ${key}`
  return headers
}

export const bellowsApi = {
  getGatewayUrl,

  async getHealth(): Promise<HealthResponse> {
    const res = await fetch(`${getGatewayUrl()}/health`, {
      headers: buildHeaders(),
      signal: AbortSignal.timeout(5000),
    })
    if (!res.ok) throw new Error(`HTTP ${res.status}`)
    return res.json() as Promise<HealthResponse>
  },

  async getModels(): Promise<ModelsResponse> {
    const res = await fetch(`${getGatewayUrl()}/v1/models`, {
      headers: buildHeaders(),
      signal: AbortSignal.timeout(5000),
    })
    if (!res.ok) throw new Error(`HTTP ${res.status}`)
    return res.json() as Promise<ModelsResponse>
  },

  async chat(
    request: ChatRequest,
    privacyMode: 'OPEN' | 'LOCAL_ONLY' = 'OPEN',
  ): Promise<ChatResponse> {
    const headers = buildHeaders(true)
    if (privacyMode === 'LOCAL_ONLY') headers['X-Anvil-Privacy'] = 'local_only'
    const res = await fetch(`${getGatewayUrl()}/v1/chat/completions`, {
      method: 'POST',
      headers,
      body: JSON.stringify(request),
      signal: AbortSignal.timeout(120_000),
    })
    if (!res.ok) {
      const body = await res.json().catch(() => ({})) as { error?: { message?: string } }
      throw new Error(body.error?.message ?? `HTTP ${res.status}`)
    }
    return res.json() as Promise<ChatResponse>
  },
}
