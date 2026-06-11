import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import {
  Clipboard, ExternalLink, Plus, Trash2, RefreshCw,
  Eye, EyeOff, Download, ChevronRight, ChevronLeft,
  Check, Copy, Wand2, Globe,
} from 'lucide-react'
import { KNOWN_PROVIDERS, makeCustomProvider, type ProviderSetup } from '../lib/knownProviders'
import { generateConfigJson, generateStartBat, generateStartSh, downloadFile } from '../lib/configExport'

const GATEWAY_KEY_ENV = 'ANVIL_BELLOWS_KEY'

function randomKey(): string {
  const arr = new Uint8Array(20)
  crypto.getRandomValues(arr)
  return Array.from(arr).map(b => b.toString(16).padStart(2, '0')).join('')
}

function loadProviders(): ProviderSetup[] {
  try { return JSON.parse(localStorage.getItem('bw_providers') ?? '[]') } catch { return [] }
}

function loadKeyValues(): Record<string, string> {
  const result: Record<string, string> = {}
  loadProviders().forEach(p => {
    const v = sessionStorage.getItem(`bw_key_${p.id}`)
    if (v) result[p.id] = v
  })
  return result
}

type Step = 'gateway' | 'providers' | 'export'

// ── Provider row ──────────────────────────────────────────────────────────────

function ProviderRow({
  provider,
  keyValue,
  expanded,
  onToggleExpand,
  onRemove,
  onKeyChange,
}: {
  provider: ProviderSetup
  keyValue: string
  expanded: boolean
  onToggleExpand: () => void
  onRemove: () => void
  onKeyChange: (val: string) => void
}) {
  const [showKey, setShowKey] = useState(false)
  const [justPasted, setJustPasted] = useState(false)

  const paste = async () => {
    try {
      const text = await navigator.clipboard.readText()
      if (text.trim()) { onKeyChange(text.trim()); setJustPasted(true); setTimeout(() => setJustPasted(false), 2000) }
    } catch { /* clipboard access denied */ }
  }

  return (
    <div className="provider-row">
      <div className="provider-row-head" onClick={onToggleExpand}>
        <div className="provider-row-name">
          <span className={`type-badge type-badge--${provider.local ? 'local' : 'cloud'}`}>
            {provider.local ? 'lokal' : 'cloud'}
          </span>
          <strong>{provider.name}</strong>
          <code className="inline-code" style={{ fontSize: 11, color: 'var(--hw-color-fg-secondary)' }}>
            {provider.id}
          </code>
        </div>
        <div className="provider-row-status">
          {provider.local ? (
            <span style={{ color: 'var(--hw-color-success)', fontSize: 12 }}>Kein Key nötig</span>
          ) : keyValue ? (
            <span style={{ color: 'var(--hw-color-success)', fontSize: 12 }}>
              <Check size={12} style={{ display: 'inline', marginRight: 3 }} />Key gesetzt
            </span>
          ) : (
            <span style={{ color: 'var(--hw-color-warning)', fontSize: 12 }}>Key fehlt</span>
          )}
          <button className="btn btn--ghost btn--sm" onClick={e => { e.stopPropagation(); onRemove() }}>
            <Trash2 size={12} />
          </button>
        </div>
      </div>

      {expanded && (
        <div className="provider-row-body">
          <div style={{ fontSize: 12, color: 'var(--hw-color-fg-secondary)', marginBottom: 8 }}>
            <code className="inline-code">{provider.baseUrl}</code>
          </div>

          {!provider.local && (
            <div className="field-group" style={{ marginBottom: 0 }}>
              <label className="field-label" style={{ fontSize: 12 }}>
                API-Key
                {provider.keyHint && (
                  <span style={{ fontWeight: 400, marginLeft: 6, color: 'var(--hw-color-fg-secondary)' }}>
                    ({provider.keyHint})
                  </span>
                )}
              </label>
              <div style={{ display: 'flex', gap: 6 }}>
                <div style={{ position: 'relative', flex: 1 }}>
                  <input
                    className="input"
                    type={showKey ? 'text' : 'password'}
                    placeholder={provider.keyHint ?? 'Key einfügen…'}
                    value={keyValue}
                    onChange={e => onKeyChange(e.target.value)}
                    style={{ paddingRight: 36, fontSize: 13 }}
                  />
                  <button
                    type="button"
                    onClick={() => setShowKey(s => !s)}
                    style={{
                      position: 'absolute', right: 8, top: '50%', transform: 'translateY(-50%)',
                      background: 'none', border: 'none', cursor: 'pointer',
                      color: 'var(--hw-color-fg-secondary)', display: 'flex', padding: 0,
                    }}
                    aria-label={showKey ? 'Key ausblenden' : 'Key anzeigen'}
                  >
                    {showKey ? <EyeOff size={14} /> : <Eye size={14} />}
                  </button>
                </div>
                <button className="btn btn--ghost btn--sm" onClick={paste} title="Aus Zwischenablage">
                  {justPasted ? <Check size={13} /> : <Clipboard size={13} />}
                </button>
                {provider.signupUrl && (
                  <a
                    href={provider.signupUrl}
                    target="_blank"
                    rel="noopener noreferrer"
                    className="btn btn--ghost btn--sm"
                    title="Zur Registrierung"
                  >
                    <ExternalLink size={13} />
                  </a>
                )}
              </div>
              <span className="field-hint" style={{ fontSize: 11 }}>
                Env-Variable: <code className="inline-code">{provider.apiKeyEnvVar}</code>
              </span>
            </div>
          )}
        </div>
      )}
    </div>
  )
}

// ── Provider picker ───────────────────────────────────────────────────────────

function ProviderPicker({
  existingIds,
  onSelect,
  onClose,
}: {
  existingIds: Set<string>
  onSelect: (p: ProviderSetup) => void
  onClose: () => void
}) {
  const [search, setSearch] = useState('')
  const [customId, setCustomId] = useState('')
  const [customUrl, setCustomUrl] = useState('')
  const [customEnv, setCustomEnv] = useState('')
  const [showCustom, setShowCustom] = useState(false)

  const filtered = KNOWN_PROVIDERS.filter(p =>
    !existingIds.has(p.id) &&
    (p.name.toLowerCase().includes(search.toLowerCase()) ||
     p.id.toLowerCase().includes(search.toLowerCase()))
  )

  const preferred = filtered.filter(p => p.category === 'preferred')
  const cloud     = filtered.filter(p => p.category === 'cloud')
  const local     = filtered.filter(p => p.category === 'local')

  const addCustom = () => {
    if (!customId.trim() || !customUrl.trim()) return
    const p = makeCustomProvider(customId.trim())
    p.baseUrl = customUrl.trim()
    if (customEnv.trim()) p.apiKeyEnvVar = customEnv.trim()
    onSelect(p)
  }

  return (
    <div className="picker-overlay" onClick={onClose}>
      <div className="picker-panel" onClick={e => e.stopPropagation()}>
        <div className="picker-head">
          <h3 style={{ margin: 0, fontSize: 15 }}>Provider hinzufügen</h3>
          <button className="btn btn--ghost btn--sm" onClick={onClose}>✕</button>
        </div>

        <input
          className="input"
          placeholder="Suchen…"
          value={search}
          onChange={e => setSearch(e.target.value)}
          style={{ marginBottom: 12 }}
          autoFocus
        />

        <div className="picker-list">
          {preferred.length > 0 && (
            <>
              <div className="picker-section-label">Bevorzugt</div>
              {preferred.map(p => (
                <button key={p.id} className="picker-item" onClick={() => onSelect({ ...p })}>
                  <span className="picker-item-name">{p.name}</span>
                  <span className="picker-item-url">{p.baseUrl}</span>
                </button>
              ))}
            </>
          )}
          {cloud.length > 0 && (
            <>
              <div className="picker-section-label">Cloud</div>
              {cloud.map(p => (
                <button key={p.id} className="picker-item" onClick={() => onSelect({ ...p })}>
                  <span className="picker-item-name">{p.name}</span>
                  <span className="picker-item-url">{p.baseUrl}</span>
                </button>
              ))}
            </>
          )}
          {local.length > 0 && (
            <>
              <div className="picker-section-label">Lokal</div>
              {local.map(p => (
                <button key={p.id} className="picker-item" onClick={() => onSelect({ ...p })}>
                  <span className="picker-item-name">{p.name}</span>
                  <span className="picker-item-url">{p.baseUrl}</span>
                  <span className={`type-badge type-badge--local`} style={{ fontSize: 10 }}>lokal</span>
                </button>
              ))}
            </>
          )}
          {filtered.length === 0 && !showCustom && (
            <div style={{ padding: '8px 0', color: 'var(--hw-color-fg-secondary)', fontSize: 13 }}>
              Kein bekannter Provider gefunden.
            </div>
          )}
        </div>

        <button
          className="btn btn--ghost btn--sm"
          style={{ marginTop: 10, width: '100%' }}
          onClick={() => setShowCustom(s => !s)}
        >
          <Plus size={13} /> Eigenen Provider
        </button>

        {showCustom && (
          <div style={{ marginTop: 10, display: 'flex', flexDirection: 'column', gap: 8 }}>
            <input
              className="input" placeholder="Provider-ID (z.B. my-provider)"
              value={customId} onChange={e => setCustomId(e.target.value)}
            />
            <input
              className="input" placeholder="Base-URL (https://… oder http://localhost:…/v1)"
              value={customUrl} onChange={e => setCustomUrl(e.target.value)}
            />
            <input
              className="input" placeholder="Env-Var für API-Key (optional)"
              value={customEnv} onChange={e => setCustomEnv(e.target.value)}
            />
            <button
              className="btn btn--primary btn--sm"
              onClick={addCustom}
              disabled={!customId.trim() || !customUrl.trim()}
            >
              Hinzufügen
            </button>
          </div>
        )}
      </div>
    </div>
  )
}

// ── CopyField helper ──────────────────────────────────────────────────────────

function CopyField({ label, value, secret }: { label: string; value: string; secret?: boolean }) {
  const [show, setShow] = useState(!secret)
  const [copied, setCopied] = useState(false)

  const copy = async () => {
    await navigator.clipboard.writeText(value)
    setCopied(true)
    setTimeout(() => setCopied(false), 2000)
  }

  return (
    <div className="copy-field">
      <span className="copy-field-label">{label}</span>
      <div className="copy-field-value">
        <code className="inline-code" style={{ flex: 1, wordBreak: 'break-all' }}>
          {show ? value : '••••••••••••'}
        </code>
        {secret && (
          <button
            type="button"
            className="btn btn--ghost btn--sm"
            onClick={() => setShow(s => !s)}
            aria-label={show ? 'Ausblenden' : 'Anzeigen'}
          >
            {show ? <EyeOff size={13} /> : <Eye size={13} />}
          </button>
        )}
        <button className="btn btn--ghost btn--sm" onClick={copy} aria-label="Kopieren">
          {copied ? <Check size={13} /> : <Copy size={13} />}
        </button>
      </div>
    </div>
  )
}

// ── Main wizard ───────────────────────────────────────────────────────────────

export default function Setup() {
  const navigate = useNavigate()

  const [step, setStep] = useState<Step>('gateway')
  const [gatewayHost, setGatewayHost] = useState(() => {
    try {
      const u = new URL(localStorage.getItem('bellows_gateway_url') ?? 'http://127.0.0.1:8765')
      return u.hostname
    } catch { return '127.0.0.1' }
  })
  const [gatewayPort, setGatewayPort] = useState<number>(() => {
    try {
      const u = new URL(localStorage.getItem('bellows_gateway_url') ?? 'http://127.0.0.1:8765')
      return parseInt(u.port) || 8765
    } catch { return 8765 }
  })
  const [gatewayKey, setGatewayKey] = useState(
    () => sessionStorage.getItem('bellows_gateway_key') || randomKey()
  )
  const [showGatewayKey, setShowGatewayKey] = useState(false)
  const [providers, setProviders]   = useState<ProviderSetup[]>(loadProviders)
  const [keyValues, setKeyValues]   = useState<Record<string, string>>(loadKeyValues)
  const [picking, setPicking]       = useState(false)
  const [expandedId, setExpandedId] = useState<string | null>(null)

  // Persist on change
  useEffect(() => {
    localStorage.setItem('bellows_gateway_url', `http://${gatewayHost}:${gatewayPort}`)
    sessionStorage.setItem('bellows_gateway_key', gatewayKey)
  }, [gatewayHost, gatewayPort, gatewayKey])

  useEffect(() => {
    localStorage.setItem('bw_providers', JSON.stringify(providers))
  }, [providers])

  useEffect(() => {
    Object.entries(keyValues).forEach(([id, val]) => {
      if (val) sessionStorage.setItem(`bw_key_${id}`, val)
      else sessionStorage.removeItem(`bw_key_${id}`)
    })
  }, [keyValues])

  const addProvider = (p: ProviderSetup) => {
    setProviders(prev => prev.find(x => x.id === p.id) ? prev : [...prev, p])
    setPicking(false)
    setExpandedId(p.id)
  }

  const removeProvider = (id: string) => {
    setProviders(prev => prev.filter(p => p.id !== id))
    setKeyValues(prev => { const n = { ...prev }; delete n[id]; return n })
    sessionStorage.removeItem(`bw_key_${id}`)
  }

  const setKey = (id: string, val: string) =>
    setKeyValues(prev => ({ ...prev, [id]: val }))

  const gatewayUrl = `http://${gatewayHost}:${gatewayPort}/v1`

  const stepIndex = ['gateway', 'providers', 'export'].indexOf(step)

  const doExport = (type: 'json' | 'bat' | 'sh') => {
    const json = generateConfigJson(providers, gatewayHost, gatewayPort, GATEWAY_KEY_ENV)
    if (type === 'json') downloadFile('bellows.config.json', json)
    else if (type === 'bat') downloadFile('start-bellows.bat', generateStartBat(providers, keyValues, gatewayKey, GATEWAY_KEY_ENV))
    else downloadFile('start-bellows.sh', generateStartSh(providers, keyValues, gatewayKey, GATEWAY_KEY_ENV))
  }

  return (
    <div className="page">
      <div className="page-header">
        <h1 className="page-title" style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
          <Wand2 size={20} /> Setup-Wizard
        </h1>
      </div>

      {/* Step indicator */}
      <div className="wizard-steps">
        {(['gateway', 'providers', 'export'] as Step[]).map((s, i) => (
          <div key={s} className={`wizard-step${step === s ? ' wizard-step--active' : ''}${i < stepIndex ? ' wizard-step--done' : ''}`}>
            <div className="wizard-step-num">{i < stepIndex ? <Check size={12} /> : i + 1}</div>
            <span className="wizard-step-label">
              {s === 'gateway' ? 'Gateway' : s === 'providers' ? 'Provider' : 'Export'}
            </span>
          </div>
        ))}
      </div>

      {/* ── Step 1: Gateway ────────────────────────────────────────────────────── */}
      {step === 'gateway' && (
        <div className="card">
          <div className="card-header">
            <h2 className="card-title">Gateway konfigurieren</h2>
          </div>
          <div className="card-body">
            <div style={{ display: 'grid', gridTemplateColumns: '1fr auto', gap: 12 }}>
              <div className="field-group">
                <label className="field-label">Host</label>
                <input className="input" value={gatewayHost} onChange={e => setGatewayHost(e.target.value)}
                  placeholder="127.0.0.1" />
              </div>
              <div className="field-group" style={{ width: 100 }}>
                <label className="field-label">Port</label>
                <input className="input" type="number" value={gatewayPort}
                  onChange={e => setGatewayPort(parseInt(e.target.value) || 8765)} min={1} max={65535} />
              </div>
            </div>

            <div className="field-group">
              <label className="field-label">
                Bellows API-Key
                <span style={{ fontWeight: 400, marginLeft: 8, color: 'var(--hw-color-fg-secondary)', fontSize: 12 }}>
                  (schützt deinen Gateway-Endpoint)
                </span>
              </label>
              <div style={{ display: 'flex', gap: 6 }}>
                <div style={{ position: 'relative', flex: 1 }}>
                  <input
                    className="input"
                    type={showGatewayKey ? 'text' : 'password'}
                    value={gatewayKey}
                    onChange={e => setGatewayKey(e.target.value)}
                    style={{ paddingRight: 36, fontFamily: 'var(--hw-font-mono)' }}
                  />
                  <button
                    type="button"
                    onClick={() => setShowGatewayKey(s => !s)}
                    style={{
                      position: 'absolute', right: 8, top: '50%', transform: 'translateY(-50%)',
                      background: 'none', border: 'none', cursor: 'pointer',
                      color: 'var(--hw-color-fg-secondary)', display: 'flex', padding: 0,
                    }}
                    aria-label={showGatewayKey ? 'Key ausblenden' : 'Key anzeigen'}
                  >
                    {showGatewayKey ? <EyeOff size={14} /> : <Eye size={14} />}
                  </button>
                </div>
                <button
                  className="btn btn--ghost btn--sm"
                  onClick={() => setGatewayKey(randomKey())}
                  title="Neu generieren"
                >
                  <RefreshCw size={13} />
                </button>
              </div>
              <span className="field-hint">
                Trage diesen Key in deiner App als API-Key ein. Base-URL:{' '}
                <code className="inline-code">{gatewayUrl}</code>
              </span>
            </div>

            <div className="info-box" style={{ marginTop: 4 }}>
              <strong>Modell-Format</strong> — trage in deiner App ein:{' '}
              <code className="inline-code">bellows/auto</code> (Router wählt bestes Modell) oder{' '}
              <code className="inline-code">bellows/gpt-4o</code> (konkretes Modell).
            </div>
          </div>
          <div className="card-footer">
            <button className="btn btn--primary" onClick={() => setStep('providers')}>
              Weiter <ChevronRight size={14} />
            </button>
          </div>
        </div>
      )}

      {/* ── Step 2: Provider ──────────────────────────────────────────────────── */}
      {step === 'providers' && (
        <div className="card">
          <div className="card-header">
            <h2 className="card-title">Provider & API-Keys</h2>
            <span className="card-count">{providers.length}</span>
          </div>
          <div className="card-body" style={{ padding: 0 }}>
            {providers.length === 0 ? (
              <div className="empty-state" style={{ padding: '32px 0' }}>
                <p>Noch kein Provider hinzugefügt.</p>
                <small>Klicke unten auf „+ Provider hinzufügen".</small>
              </div>
            ) : (
              <div className="provider-list">
                {providers.map(p => (
                  <ProviderRow
                    key={p.id}
                    provider={p}
                    keyValue={keyValues[p.id] ?? ''}
                    expanded={expandedId === p.id}
                    onToggleExpand={() => setExpandedId(prev => prev === p.id ? null : p.id)}
                    onRemove={() => removeProvider(p.id)}
                    onKeyChange={val => setKey(p.id, val)}
                  />
                ))}
              </div>
            )}
          </div>
          <div className="card-footer" style={{ flexDirection: 'column', gap: 10, alignItems: 'stretch' }}>
            <button className="btn btn--ghost btn--sm" onClick={() => setPicking(true)}>
              <Plus size={13} /> Provider hinzufügen
            </button>
            <div style={{ display: 'flex', gap: 8, justifyContent: 'space-between' }}>
              <button className="btn btn--ghost btn--sm" onClick={() => setStep('gateway')}>
                <ChevronLeft size={14} /> Zurück
              </button>
              <button className="btn btn--primary" onClick={() => setStep('export')}>
                Weiter <ChevronRight size={14} />
              </button>
            </div>
          </div>
        </div>
      )}

      {/* ── Step 3: Export ────────────────────────────────────────────────────── */}
      {step === 'export' && (
        <>
          <div className="card">
            <div className="card-header">
              <h2 className="card-title">Verbindungs-Info für externe Apps</h2>
            </div>
            <div className="card-body">
              <p className="card-prose" style={{ marginBottom: 12 }}>
                Trage diese Werte in OpenCode, Cursor, Continue.dev oder jede andere
                OpenAI-kompatible App ein:
              </p>
              <CopyField label="API Base URL" value={gatewayUrl} />
              <CopyField label="API Key (Bellows)" value={gatewayKey} secret />
              <CopyField label="Modell — Auto-Routing" value="bellows/auto" />
            </div>
          </div>

          <div className="card">
            <div className="card-header">
              <h2 className="card-title">Konfiguration herunterladen</h2>
            </div>
            <div className="card-body">
              <p className="card-prose">
                Lade beide Dateien herunter und lege sie in dasselbe Verzeichnis wie{' '}
                <code className="inline-code">bellows.bat</code> bzw.{' '}
                <code className="inline-code">bellows</code>.
                Dann das Start-Script einmal ausführen — danach läuft der Gateway automatisch.
              </p>
              <div style={{ display: 'flex', gap: 8, flexWrap: 'wrap', marginTop: 12 }}>
                <button className="btn btn--primary btn--sm" onClick={() => doExport('json')}>
                  <Download size={13} /> bellows.config.json
                </button>
                <button className="btn btn--ghost btn--sm" onClick={() => doExport('bat')}>
                  <Download size={13} /> start-bellows.bat
                  <span style={{ marginLeft: 4, fontSize: 11, color: 'var(--hw-color-fg-secondary)' }}>Windows</span>
                </button>
                <button className="btn btn--ghost btn--sm" onClick={() => doExport('sh')}>
                  <Download size={13} /> start-bellows.sh
                  <span style={{ marginLeft: 4, fontSize: 11, color: 'var(--hw-color-fg-secondary)' }}>Mac/Linux</span>
                </button>
              </div>
            </div>
          </div>

          <div className="card">
            <div className="card-header">
              <h2 className="card-title">Nächste Schritte</h2>
            </div>
            <div className="card-body">
              <ol style={{ margin: 0, paddingLeft: 20, lineHeight: 2 }}>
                <li>
                  <code className="inline-code">bellows.config.json</code> und Start-Script herunterladen
                </li>
                <li>Beide Dateien neben <code className="inline-code">bellows.bat</code> ablegen</li>
                <li>
                  <code className="inline-code">start-bellows.bat</code> (Win) oder{' '}
                  <code className="inline-code">chmod +x start-bellows.sh && ./start-bellows.sh</code> ausführen
                </li>
                <li>Auf „Verbinden" klicken</li>
              </ol>
            </div>
          </div>

          <div style={{ display: 'flex', gap: 8, justifyContent: 'space-between', marginTop: 4 }}>
            <button className="btn btn--ghost" onClick={() => setStep('providers')}>
              <ChevronLeft size={14} /> Zurück
            </button>
            <button className="btn btn--primary" onClick={() => navigate('/')}>
              <Globe size={14} /> Verbinden
            </button>
          </div>
        </>
      )}

      {picking && (
        <ProviderPicker
          existingIds={new Set(providers.map(p => p.id))}
          onSelect={addProvider}
          onClose={() => setPicking(false)}
        />
      )}
    </div>
  )
}
