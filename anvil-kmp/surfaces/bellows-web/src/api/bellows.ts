export type QualityState = 'STABLE' | 'DEGRADED' | 'BLOCKED' | 'FAILED'
export type BellowsErrorKind = 'network' | 'auth' | 'server' | 'client'

export class BellowsError extends Error {
  constructor(
    message: string,
    public readonly kind: BellowsErrorKind,
    public readonly status?: number,
  ) {
    super(message)
    this.name = 'BellowsError'
  }
}

export interface HealthResponse {
  status: QualityState
  adapters: Record<string, QualityState>
  models: string[]
}

export interface ModelCard { id: string; object: string; owned_by?: string }
export interface ModelsResponse { object: string; data: ModelCard[] }

export interface ChatMessage { role: 'user' | 'assistant' | 'system'; content: string }
export interface ChatRequest {
  model?: string; messages: ChatMessage[]
  max_tokens?: number; temperature?: number; stream?: boolean
}
export interface ChatResponse {
  id: string; object: string; model: string
  choices: Array<{ index: number; message: ChatMessage; finish_reason: string }>
  usage?: { prompt_tokens: number; completion_tokens: number; total_tokens: number }
}

export function getGatewayUrl(): string {
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

function throwForStatus(status: number, body: { error?: { message?: string } }): never {
  const msg = body.error?.message ?? `HTTP ${status}`
  if (status === 401 || status === 403) throw new BellowsError(msg, 'auth', status)
  if (status >= 500) throw new BellowsError(msg, 'server', status)
  throw new BellowsError(msg, 'client', status)
}

async function safeFetch(url: string, options: RequestInit): Promise<Response> {
  try {
    return await fetch(url, options)
  } catch (e) {
    const msg = e instanceof Error && e.name === 'TimeoutError'
      ? 'Zeitüberschreitung'
      : 'Gateway nicht erreichbar'
    throw new BellowsError(msg, 'network')
  }
}

export const bellowsApi = {
  getGatewayUrl,

  async getHealth(): Promise<HealthResponse> {
    const res = await safeFetch(`${getGatewayUrl()}/health`, {
      headers: buildHeaders(),
      signal: AbortSignal.timeout(5000),
    })
    if (!res.ok) {
      const body = await res.json().catch(() => ({})) as { error?: { message?: string } }
      throwForStatus(res.status, body)
    }
    return res.json() as Promise<HealthResponse>
  },

  async getModels(): Promise<ModelsResponse> {
    const res = await safeFetch(`${getGatewayUrl()}/v1/models`, {
      headers: buildHeaders(),
      signal: AbortSignal.timeout(5000),
    })
    if (!res.ok) {
      const body = await res.json().catch(() => ({})) as { error?: { message?: string } }
      throwForStatus(res.status, body)
    }
    return res.json() as Promise<ModelsResponse>
  },

  async chat(request: ChatRequest, privacyMode: 'OPEN' | 'LOCAL_ONLY' = 'OPEN'): Promise<ChatResponse> {
    const headers = buildHeaders(true)
    if (privacyMode === 'LOCAL_ONLY') headers['X-Anvil-Privacy'] = 'local_only'
    const res = await safeFetch(`${getGatewayUrl()}/v1/chat/completions`, {
      method: 'POST',
      headers,
      body: JSON.stringify(request),
      signal: AbortSignal.timeout(120_000),
    })
    if (!res.ok) {
      const body = await res.json().catch(() => ({})) as { error?: { message?: string } }
      throwForStatus(res.status, body)
    }
    return res.json() as Promise<ChatResponse>
  },

  /** Push a new config + resolved API keys to the running gateway without restarting. */
  async hotReload(
    config: object,
    resolvedKeys: Record<string, string>,
  ): Promise<{ status: string; providers: number }> {
    const res = await safeFetch(`${getGatewayUrl()}/admin/hot-reload`, {
      method: 'POST',
      headers: buildHeaders(true),
      body: JSON.stringify({ config, resolvedKeys }),
      signal: AbortSignal.timeout(10_000),
    })
    if (!res.ok) {
      const body = await res.json().catch(() => ({})) as { error?: { message?: string } }
      throwForStatus(res.status, body)
    }
    return res.json() as Promise<{ status: string; providers: number }>
  },

  /** Signal the gateway to restart. The start-script restart-loop brings it back up. */
  async restart(): Promise<void> {
    try {
      await safeFetch(`${getGatewayUrl()}/admin/restart`, {
        method: 'POST',
        headers: buildHeaders(true),
        signal: AbortSignal.timeout(5_000),
      })
    } catch (e) {
      // Network error is expected: process exits before the response is flushed
      if (!(e instanceof BellowsError && e.kind === 'network')) throw e
    }
  },
}
