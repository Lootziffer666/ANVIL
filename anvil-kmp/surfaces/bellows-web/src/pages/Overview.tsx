import { useCallback, useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { RefreshCw, AlertCircle, Wifi, Wand2 } from 'lucide-react'
import { bellowsApi, BellowsError, type HealthResponse } from '../api/bellows'
import StatusBadge from '../components/StatusBadge'

interface ErrorState { message: string; kind: 'network' | 'auth' | 'server' | 'client' }

export default function Overview() {
  const navigate = useNavigate()
  const [health, setHealth]       = useState<HealthResponse | null>(null)
  const [loading, setLoading]     = useState(true)
  const [error, setError]         = useState<ErrorState | null>(null)
  const [updatedAt, setUpdatedAt] = useState<Date | null>(null)

  const load = useCallback(async () => {
    setLoading(true)
    setError(null)
    try {
      const h = await bellowsApi.getHealth()
      setHealth(h)
      setUpdatedAt(new Date())
    } catch (e) {
      setError(
        e instanceof BellowsError
          ? { message: e.message, kind: e.kind }
          : { message: e instanceof Error ? e.message : 'Unbekannt', kind: 'network' },
      )
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => { load() }, [load])

  const adapters     = health?.adapters ? Object.entries(health.adapters) : []
  const stableCount  = adapters.filter(([, s]) => s === 'STABLE').length
  const localModels  = (health?.models ?? []).filter(
    m => m.includes('local') || m.includes('hermes') || m.includes('llama') || m.includes('ollama'),
  )

  return (
    <div className="page">
      <div className="page-header">
        <h1 className="page-title">Overview</h1>
        <button className="btn btn--ghost btn--sm" onClick={load} disabled={loading}>
          <RefreshCw size={13} className={loading ? 'spin' : ''} />
          Refresh
        </button>
      </div>

      {error ? (
        <div className="alert alert--error">
          <AlertCircle size={16} style={{ flexShrink: 0 }} />
          <div style={{ flex: 1 }}>
            {error.kind === 'auth' ? (
              <>
                <strong>Falscher API-Key</strong> — der Gateway läuft, aber der Bearer-Token stimmt nicht überein.
                <br />
                <span className="alert-hint">
                  Key in <a href="#" onClick={e => { e.preventDefault(); navigate('/setup') }}>Setup</a> prüfen oder in{' '}
                  <a href="#" onClick={e => { e.preventDefault(); navigate('/config') }}>Config</a> korrigieren.
                </span>
              </>
            ) : (
              <>
                <strong>Gateway nicht erreichbar</strong>{error.message !== 'Gateway nicht erreichbar' ? ` — ${error.message}` : ''}
                <br />
                <span className="alert-hint">
                  Falsche URL oder Gateway nicht gestartet — starte{' '}
                  <code>start-bellows.bat</code> / <code>start-bellows.sh</code> aus dem Setup.
                </span>
              </>
            )}
          </div>
          <button
            className="btn btn--primary btn--sm"
            onClick={() => navigate('/setup')}
            style={{ flexShrink: 0, gap: 6 }}
          >
            <Wand2 size={13} />
            Jetzt einrichten
          </button>
        </div>
      ) : (
        <>
          {/* Status banner */}
          <div className={`status-banner status-banner--${(health?.status ?? 'loading').toLowerCase()}`}>
            <div className="status-banner-left">
              <Wifi size={18} />
              <div>
                <div className="status-banner-label">Router Status</div>
                <div className="status-banner-desc">
                  {bellowsApi.getGatewayUrl()}
                </div>
              </div>
            </div>
            <div className="status-banner-right">
              {health
                ? <StatusBadge state={health.status} />
                : <span className="skeleton" style={{ width: 72, height: 22, display: 'inline-block' }} />
              }
              {updatedAt && (
                <span className="status-banner-time">
                  {updatedAt.toLocaleTimeString()}
                </span>
              )}
            </div>
          </div>

          {/* Stats row */}
          <div className="stats-grid">
            <StatCard label="Adapters"       value={adapters.length}         loading={loading} />
            <StatCard label="Healthy"        value={stableCount}             loading={loading} accent="success" />
            <StatCard label="Models"         value={health?.models.length}   loading={loading} />
            <StatCard label="Local models"   value={localModels.length}      loading={loading} accent="info" />
          </div>

          {/* Adapters table */}
          {adapters.length > 0 && (
            <div className="card">
              <div className="card-header">
                <h2 className="card-title">Adapters</h2>
                <span className="card-count">{adapters.length}</span>
              </div>
              <table className="table">
                <thead>
                  <tr>
                    <th>ID</th>
                    <th>State</th>
                  </tr>
                </thead>
                <tbody>
                  {adapters.map(([id, state]) => (
                    <tr key={id}>
                      <td><code className="inline-code">{id}</code></td>
                      <td><StatusBadge state={state} size="sm" /></td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}

          {/* Model list (collapsed preview) */}
          {health && health.models.length > 0 && (
            <div className="card">
              <div className="card-header">
                <h2 className="card-title">Available Models</h2>
                <span className="card-count">{health.models.length}</span>
              </div>
              <div className="model-pill-list">
                {health.models.map(m => (
                  <span key={m} className="model-pill">{m}</span>
                ))}
              </div>
            </div>
          )}
        </>
      )}
    </div>
  )
}

function StatCard({
  label,
  value,
  loading,
  accent,
}: {
  label: string
  value: number | undefined
  loading: boolean
  accent?: 'success' | 'info'
}) {
  const colorMap = { success: 'var(--hw-color-success)', info: 'var(--hw-color-info)' }
  const color = accent ? colorMap[accent] : 'var(--hw-color-fg-primary)'
  return (
    <div className="stat-card">
      <div className="stat-label">{label}</div>
      {loading || value === undefined ? (
        <span className="skeleton" style={{ width: 48, height: 32, display: 'block', marginTop: 4 }} />
      ) : (
        <div className="stat-value" style={{ color }}>{value}</div>
      )}
    </div>
  )
}
