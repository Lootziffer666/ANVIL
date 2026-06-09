import { useCallback, useEffect, useState } from 'react'
import { RefreshCw, Server, AlertCircle } from 'lucide-react'
import { bellowsApi, type HealthResponse } from '../api/bellows'
import StatusBadge from '../components/StatusBadge'

export default function Providers() {
  const [health, setHealth]   = useState<HealthResponse | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError]     = useState<string | null>(null)

  const load = useCallback(async () => {
    setLoading(true)
    setError(null)
    try {
      const h = await bellowsApi.getHealth()
      setHealth(h)
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Unreachable')
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => { load() }, [load])

  const adapters = health ? Object.entries(health.adapters) : []

  const modelsByAdapter = (id: string) =>
    (health?.models ?? []).filter(m => {
      const prefix = m.split('/')[0]
      return prefix === id || m.toLowerCase().includes(id.toLowerCase())
    })

  const isLocal = (id: string) =>
    ['local', 'lmstudio', 'ollama', 'llamacpp', 'vllm', 'hermes'].some(k =>
      id.toLowerCase().includes(k),
    )

  return (
    <div className="page">
      <div className="page-header">
        <h1 className="page-title">Providers</h1>
        <button className="btn btn--ghost btn--sm" onClick={load} disabled={loading}>
          <RefreshCw size={13} className={loading ? 'spin' : ''} />
          Refresh
        </button>
      </div>

      {error ? (
        <div className="alert alert--error">
          <AlertCircle size={16} style={{ flexShrink: 0 }} />
          <div>
            <strong>Gateway unreachable</strong> — {error}
          </div>
        </div>
      ) : loading ? (
        <div className="loading-state">
          <RefreshCw size={28} className="spin" style={{ color: 'var(--hw-color-accent)' }} />
          <span>Loading providers…</span>
        </div>
      ) : adapters.length === 0 ? (
        <div className="empty-state">
          <Server size={48} style={{ color: 'var(--hw-color-border)' }} />
          <p>No providers configured</p>
          <small>Add entries to your <code className="inline-code">bellows.json</code></small>
        </div>
      ) : (
        <div className="card">
          <div className="card-header">
            <h2 className="card-title">Configured Adapters</h2>
            <span className="card-count">{adapters.length}</span>
          </div>
          <table className="table">
            <thead>
              <tr>
                <th>Provider ID</th>
                <th>Type</th>
                <th>Models</th>
                <th>State</th>
              </tr>
            </thead>
            <tbody>
              {adapters.map(([id, state]) => {
                const local    = isLocal(id)
                const mods     = modelsByAdapter(id)
                return (
                  <tr key={id}>
                    <td>
                      <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
                        <Server size={14} style={{ color: 'var(--hw-color-accent)', flexShrink: 0 }} />
                        <code className="inline-code">{id}</code>
                      </div>
                    </td>
                    <td>
                      <span className={`type-badge type-badge--${local ? 'local' : 'cloud'}`}>
                        {local ? 'local' : 'cloud'}
                      </span>
                    </td>
                    <td>
                      <span style={{ fontSize: 13, color: 'var(--hw-color-fg-secondary)' }}>
                        {mods.length > 0 ? mods.length : '—'}
                      </span>
                    </td>
                    <td>
                      <StatusBadge state={state} size="sm" />
                    </td>
                  </tr>
                )
              })}
            </tbody>
          </table>
        </div>
      )}

      <div className="info-box">
        <strong>Privacy guarantee</strong> — adapters with type <em>local</em> are the only
        ones eligible when <code className="inline-code">PrivacyMode.LOCAL_ONLY</code> is set.
        Cloud adapters are never contacted as a fallback.
      </div>
    </div>
  )
}
