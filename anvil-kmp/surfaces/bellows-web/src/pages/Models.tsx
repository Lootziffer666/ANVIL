import { useCallback, useEffect, useState } from 'react'
import { RefreshCw, Search, Cpu, AlertCircle } from 'lucide-react'
import { bellowsApi, type ModelCard } from '../api/bellows'

const LOCAL_HINTS = ['local', 'hermes', 'llama', 'ollama', 'lmstudio', 'mistral-local', 'phi', 'gemma']

function guessLocal(id: string) {
  return LOCAL_HINTS.some(h => id.toLowerCase().includes(h))
}

function guessProvider(model: ModelCard): string {
  if (model.owned_by && model.owned_by !== 'openai') return model.owned_by
  if (model.id.includes('/')) return model.id.split('/')[0]
  if (model.id.startsWith('gpt-') || model.id.startsWith('o1') || model.id.startsWith('o3'))
    return 'openai'
  if (model.id.startsWith('claude-')) return 'anthropic'
  if (model.id.startsWith('gemini-')) return 'google'
  return '—'
}

export default function Models() {
  const [models, setModels]   = useState<ModelCard[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError]     = useState<string | null>(null)
  const [query, setQuery]     = useState('')

  const load = useCallback(async () => {
    setLoading(true)
    setError(null)
    try {
      const m = await bellowsApi.getModels()
      setModels(m.data)
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Unreachable')
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => { load() }, [load])

  const filtered = models.filter(m =>
    m.id.toLowerCase().includes(query.toLowerCase()),
  )

  const localCount = filtered.filter(m => guessLocal(m.id)).length
  const cloudCount = filtered.length - localCount

  return (
    <div className="page">
      <div className="page-header">
        <h1 className="page-title">Models</h1>
        <button className="btn btn--ghost btn--sm" onClick={load} disabled={loading}>
          <RefreshCw size={13} className={loading ? 'spin' : ''} />
          Refresh
        </button>
      </div>

      <div className="search-bar">
        <Search size={15} />
        <input
          className="search-input"
          placeholder="Filter models…"
          value={query}
          onChange={e => setQuery(e.target.value)}
        />
        {filtered.length > 0 && (
          <span className="search-count">
            {localCount > 0 && <span className="type-badge type-badge--local">{localCount} local</span>}
            {cloudCount > 0 && <span className="type-badge type-badge--cloud">{cloudCount} cloud</span>}
          </span>
        )}
      </div>

      {error ? (
        <div className="alert alert--error">
          <AlertCircle size={16} style={{ flexShrink: 0 }} />
          <div><strong>Gateway unreachable</strong> — {error}</div>
        </div>
      ) : loading ? (
        <div className="loading-state">
          <RefreshCw size={28} className="spin" style={{ color: 'var(--hw-color-accent)' }} />
          <span>Loading models…</span>
        </div>
      ) : filtered.length === 0 ? (
        <div className="empty-state">
          <Cpu size={48} style={{ color: 'var(--hw-color-border)' }} />
          <p>{query ? 'No models match your filter' : 'No models available'}</p>
          {!query && <small>Check that your providers are configured and healthy</small>}
        </div>
      ) : (
        <div className="model-grid">
          {filtered.map(model => {
            const local    = guessLocal(model.id)
            const provider = guessProvider(model)
            return (
              <div key={model.id} className="model-card">
                <div className="model-card-top">
                  <Cpu size={14} style={{ color: 'var(--hw-color-accent)' }} />
                  <span className={`type-badge type-badge--${local ? 'local' : 'cloud'}`}>
                    {local ? 'local' : 'cloud'}
                  </span>
                </div>
                <div className="model-id">{model.id}</div>
                <div className="model-provider">{provider}</div>
              </div>
            )
          })}
        </div>
      )}
    </div>
  )
}
