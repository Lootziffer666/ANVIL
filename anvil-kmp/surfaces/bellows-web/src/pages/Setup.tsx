import { useState, useEffect, useRef, useCallback } from 'react'
import { useNavigate } from 'react-router-dom'
import {
  Clipboard, ExternalLink, Plus, Trash2, RefreshCw,
  Eye, EyeOff, Download, ChevronRight, ChevronLeft,
  Check, Copy, Wand2, Globe, Search, Zap,
} from 'lucide-react'
import { KNOWN_PROVIDERS, makeCustomProvider, type ProviderSetup } from '../lib/knownProviders'
import { generateConfigJson, generateStartBat, generateStartSh, downloadFile } from '../lib/configExport'
import { bellowsApi, BellowsError } from '../api/bellows'

const GATEWAY_KEY_ENV = 'ANVIL_BELLOWS_KEY'
const KNOWN_IDS = new Set(KNOWN_PROVIDERS.map(p => p.id))

function randomKey(): string {
  const arr = new Uint8Array(20)
  crypto.getRandomValues(arr)
  return Array.from(arr).map(b => b.toString(16).padStart(2, '0')).join('')
}

// Provider API keys persist in localStorage — they end up in a shell script anyway.
// Only the gateway Bearer token stays in sessionStorage (higher security requirement).
function loadKey(id: string): string {
  return localStorage.getItem(`bw_key_${id}`) ?? ''
}
function saveKey(id: string, val: string): void {
  if (val) localStorage.setItem(`bw_key_${id}`, val)
  else localStorage.removeItem(`bw_key_${id}`)
}

function loadProviders(): ProviderSetup[] {
  try {
    const saved = JSON.parse(localStorage.getItem('bw_providers') ?? '[]') as ProviderSetup[]
    if (saved.length > 0) return saved
  } catch { /* fall through */ }
  return KNOWN_PROVIDERS.map(p => ({ ...p }))
}

function loadSelectedIds(): Set<string> {
  try { return new Set(JSON.parse(localStorage.getItem('bw_selected') ?? '[]') as string[]) }
  catch { return new Set() }
}

function loadKeyValues(providers: ProviderSetup[]): Record<string, string> {
  const result: Record<string, string> = {}
  providers.forEach(p => { const v = loadKey(p.id); if (v) result[p.id] = v })
  return result
}

// A key looks like an API key (not a URL, not whitespace-containing, not trivially short)
function looksLikeKey(text: string): boolean {
  if (text.length < 8 || text.length > 512) return false
  if (/\s/.test(text)) return false
  if (text.startsWith('http')) return false
  return /[^a-zA-Z0-9]/.test(text) || text.length >= 20
}

type Step = 'select' | 'wizard' | 'export'

// ── CopyCmd: a shell command shown with a copy button ─────────────────────────
function CopyCmd({ cmd }: { cmd: string }) {
  const [copied, setCopied] = useState(false)
  const copy = () => {
    navigator.clipboard.writeText(cmd).catch(() => {})
    setCopied(true)
    setTimeout(() => setCopied(false), 2000)
  }
  return (
    <div className="copy-cmd">
      <code className="copy-cmd-text">{cmd}</code>
      <button className="btn btn--ghost btn--sm" onClick={copy} title="Kopieren">
        {copied ? <Check size={13} /> : <Copy size={13} />}
      </button>
    </div>
  )
}

// ── CopyField ─────────────────────────────────────────────────────────────────
function CopyField({ label, value, secret }: { label: string; value: string; secret?: boolean }) {
  const [show, setShow] = useState(!secret)
  const [copied, setCopied] = useState(false)
  const copy = () => {
    navigator.clipboard.writeText(value).catch(() => {})
    setCopied(true)
    setTimeout(() => setCopied(false), 2000)
  }
  return (
    <div className="copy-field">
      <span className="copy-field-label">{label}</span>
      <div style={{ flex: 1, fontFamily: 'var(--hw-font-mono)', fontSize: 13, wordBreak: 'break-all' }}>
        {show ? value : '••••••••••••'}
      </div>
      {secret && (
        <button type="button" className="btn btn--ghost btn--sm" onClick={() => setShow(s => !s)} aria-label={show ? 'Ausblenden' : 'Anzeigen'}>
          {show ? <EyeOff size={13} /> : <Eye size={13} />}
        </button>
      )}
      <button className="btn btn--ghost btn--sm" onClick={copy} aria-label="Kopieren">
        {copied ? <Check size={13} /> : <Copy size={13} />}
      </button>
    </div>
  )
}

// ── Main wizard ───────────────────────────────────────────────────────────────
export default function Setup() {
  const navigate = useNavigate()

  const [allProviders, setAllProviders] = useState<ProviderSetup[]>(loadProviders)
  const [selectedIds, setSelectedIds]   = useState<Set<string>>(loadSelectedIds)
  const [keyValues, setKeyValues]       = useState<Record<string, string>>(() => loadKeyValues(loadProviders()))

  const [step, setStep]           = useState<Step>('select')
  const [search, setSearch]       = useState('')
  const [wizardIndex, setWizardIndex] = useState(0)
  const [clipStatus, setClipStatus] = useState<'waiting' | 'detected'>('waiting')

  // Export step: live gateway interaction
  const [gwOnline, setGwOnline]     = useState<boolean | null>(null)
  const [applyState, setApplyState] = useState<'idle' | 'busy' | 'ok' | 'err'>('idle')
  const [applyMsg, setApplyMsg]     = useState('')
  const [restartState, setRestartState] = useState<'idle' | 'busy' | 'waiting' | 'ok' | 'err'>('idle')

  const [gatewayHost, setGatewayHost] = useState(() => {
    try { return new URL(localStorage.getItem('bellows_gateway_url') ?? 'http://127.0.0.1:8765').hostname }
    catch { return '127.0.0.1' }
  })
  const [gatewayPort, setGatewayPort] = useState<number>(() => {
    try { return parseInt(new URL(localStorage.getItem('bellows_gateway_url') ?? 'http://127.0.0.1:8765').port) || 8765 }
    catch { return 8765 }
  })
  const [gatewayKey, setGatewayKey]     = useState(() => sessionStorage.getItem('bellows_gateway_key') || randomKey())
  const [showGatewayKey, setShowGatewayKey] = useState(false)

  const [showCustomForm, setShowCustomForm] = useState(false)
  const [customId, setCustomId]   = useState('')
  const [customUrl, setCustomUrl] = useState('')
  const [customEnv, setCustomEnv] = useState('')

  const clipStopRef = useRef(false)

  // Persist state
  useEffect(() => {
    localStorage.setItem('bellows_gateway_url', `http://${gatewayHost}:${gatewayPort}`)
    sessionStorage.setItem('bellows_gateway_key', gatewayKey)
  }, [gatewayHost, gatewayPort, gatewayKey])

  useEffect(() => { localStorage.setItem('bw_providers', JSON.stringify(allProviders)) }, [allProviders])
  useEffect(() => { localStorage.setItem('bw_selected', JSON.stringify([...selectedIds])) }, [selectedIds])

  // Derived
  const wizardProviders     = allProviders.filter(p => selectedIds.has(p.id) && !p.local)
  const currentProvider     = wizardProviders[wizardIndex] ?? null
  const selectedProviders   = allProviders.filter(p => selectedIds.has(p.id))

  // Skip wizard if nothing needs keys
  useEffect(() => {
    if (step === 'wizard' && (!currentProvider)) setStep('export')
  }, [step, currentProvider])

  // Clipboard polling during wizard
  useEffect(() => {
    if (step !== 'wizard' || !currentProvider) return
    const pId    = currentProvider.id
    const pCount = wizardProviders.length
    const idx    = wizardIndex

    clipStopRef.current = false
    setClipStatus('waiting')
    let lastClip = ''
    let advTimer: ReturnType<typeof setTimeout> | null = null

    const poll = async () => {
      if (clipStopRef.current) return
      try {
        const text = (await navigator.clipboard.readText()).trim()
        if (text && text !== lastClip && looksLikeKey(text)) {
          lastClip = text
          setClipStatus('detected')
          saveKey(pId, text)
          setKeyValues(prev => ({ ...prev, [pId]: text }))
          advTimer = setTimeout(() => {
            if (clipStopRef.current) return
            setClipStatus('waiting')
            if (idx + 1 < pCount) setWizardIndex(idx + 1)
            else setStep('export')
          }, 1500)
          return
        }
      } catch { /* permission denied */ }
      setTimeout(poll, 600)
    }
    poll()
    return () => { clipStopRef.current = true; if (advTimer) clearTimeout(advTimer) }
  }, [step, wizardIndex, currentProvider?.id]) // eslint-disable-line react-hooks/exhaustive-deps

  const setKey = useCallback((id: string, val: string) => {
    saveKey(id, val)
    setKeyValues(prev => ({ ...prev, [id]: val }))
  }, [])

  const manualAdvance = () => {
    clipStopRef.current = true
    setClipStatus('waiting')
    if (wizardIndex + 1 < wizardProviders.length) setWizardIndex(i => i + 1)
    else setStep('export')
  }

  const addCustom = () => {
    if (!customId.trim() || !customUrl.trim()) return
    const p = makeCustomProvider(customId.trim())
    p.baseUrl = customUrl.trim()
    if (customEnv.trim()) p.apiKeyEnvVar = customEnv.trim()
    setAllProviders(prev => prev.find(x => x.id === p.id) ? prev : [...prev, p])
    setSelectedIds(prev => new Set([...prev, p.id]))
    setCustomId(''); setCustomUrl(''); setCustomEnv('')
    setShowCustomForm(false)
  }

  const removeCustomProvider = (id: string) => {
    setAllProviders(prev => prev.filter(p => p.id !== id))
    setSelectedIds(prev => { const n = new Set(prev); n.delete(id); return n })
    setKey(id, '')
  }

  const toggleSelect  = (id: string) => setSelectedIds(prev => { const n = new Set(prev); n.has(id) ? n.delete(id) : n.add(id); return n })
  const selectAll     = () => setSelectedIds(new Set(allProviders.map(p => p.id)))
  const selectNone    = () => setSelectedIds(new Set())
  const selectCloud   = () => setSelectedIds(new Set(allProviders.filter(p => !p.local).map(p => p.id)))
  const selectLocal   = () => setSelectedIds(new Set(allProviders.filter(p => p.local).map(p => p.id)))

  // Poll gateway when export step is visible
  useEffect(() => {
    if (step !== 'export') return
    setGwOnline(null)
    bellowsApi.getHealth()
      .then(() => setGwOnline(true))
      .catch(() => setGwOnline(false))
  }, [step])

  const applyToGateway = async () => {
    setApplyState('busy')
    setApplyMsg('')
    try {
      const configObj = JSON.parse(generateConfigJson(selectedProviders, gatewayHost, gatewayPort, GATEWAY_KEY_ENV)) as object
      const resolvedKeys: Record<string, string> = {}
      selectedProviders.forEach(p => { if (!p.local && keyValues[p.id]) resolvedKeys[p.apiKeyEnvVar] = keyValues[p.id] })
      const result = await bellowsApi.hotReload(configObj, resolvedKeys)
      setApplyState('ok')
      setApplyMsg(`${result.providers} Provider aktiv`)
      setGwOnline(true)
    } catch (e) {
      setApplyState('err')
      const err = e instanceof BellowsError ? e : null
      if (err?.kind === 'auth') {
        setApplyMsg('Falscher API-Key — Gateway läuft mit einem anderen Bearer-Token. Starte den Gateway mit dem neuen Start-Script neu.')
      } else if (err?.kind === 'network') {
        setApplyMsg('Gateway nicht erreichbar — starte zuerst start-bellows.bat / start-bellows.sh.')
        setGwOnline(false)
      } else {
        setApplyMsg(err?.message ?? 'Unbekannter Fehler')
      }
    }
  }

  const restartGateway = async () => {
    setRestartState('busy')
    try {
      await bellowsApi.restart()
    } catch { /* network error expected */ }
    setRestartState('waiting')
    // Poll until gateway is back
    const t0 = Date.now()
    const poll = async (): Promise<void> => {
      if (Date.now() - t0 > 30_000) { setRestartState('err'); return }
      try {
        await bellowsApi.getHealth()
        setRestartState('ok')
        setGwOnline(true)
        return
      } catch { /* not up yet */ }
      setTimeout(poll, 1000)
    }
    setTimeout(poll, 1500)
  }

  const doExport = (type: 'json' | 'bat' | 'sh') => {
    const json = generateConfigJson(selectedProviders, gatewayHost, gatewayPort, GATEWAY_KEY_ENV)
    if (type === 'json') return downloadFile('bellows.config.json', json)
    if (type === 'bat')  return downloadFile('start-bellows.bat', generateStartBat(selectedProviders, keyValues, gatewayKey, GATEWAY_KEY_ENV))
    downloadFile('start-bellows.sh', generateStartSh(selectedProviders, keyValues, gatewayKey, GATEWAY_KEY_ENV))
  }

  const gatewayUrl = `http://${gatewayHost}:${gatewayPort}/v1`
  const stepIndex  = ['select', 'wizard', 'export'].indexOf(step)

  const filtered  = allProviders.filter(p => !search || p.name.toLowerCase().includes(search.toLowerCase()) || p.id.toLowerCase().includes(search.toLowerCase()))
  const preferred = filtered.filter(p => p.category === 'preferred' && KNOWN_IDS.has(p.id))
  const cloud     = filtered.filter(p => p.category === 'cloud' && KNOWN_IDS.has(p.id))
  const local     = filtered.filter(p => p.local && KNOWN_IDS.has(p.id))
  const custom    = filtered.filter(p => !KNOWN_IDS.has(p.id))

  const needsKeys = wizardProviders.filter(p => !keyValues[p.id])

  // ── render helpers ───────────────────────────────────────────────────────────

  const renderProviderGroup = (label: string, items: ProviderSetup[]) => {
    if (items.length === 0) return null
    return (
      <div key={label}>
        <div className="picker-section-label">{label}</div>
        {items.map(p => {
          const isSelected = selectedIds.has(p.id)
          const hasKey     = !!keyValues[p.id]
          const isCustom   = !KNOWN_IDS.has(p.id)
          return (
            <div key={p.id} className={`provider-select-row${isSelected ? ' provider-select-row--on' : ''}`}>
              <label className="provider-select-check">
                <input
                  type="checkbox"
                  checked={isSelected}
                  onChange={() => toggleSelect(p.id)}
                  style={{ width: 15, height: 15, accentColor: 'var(--hw-color-accent)', flexShrink: 0 }}
                />
                <div className="provider-select-info">
                  <div className="provider-select-name">
                    {p.name}
                    {hasKey && <span className="provider-key-badge provider-key-badge--set"><Check size={10} /> Key</span>}
                    {!hasKey && !p.local && isSelected && <span className="provider-key-badge provider-key-badge--miss">kein Key</span>}
                    {p.local && <span className="provider-key-badge provider-key-badge--local">lokal</span>}
                  </div>
                  <div className="provider-select-url">{p.baseUrl}</div>
                </div>
              </label>

              {/* Inline key input — only when selected and not local */}
              {isSelected && !p.local && (
                <div className="provider-key-inline" onClick={e => e.stopPropagation()}>
                  <input
                    className="input"
                    type="password"
                    placeholder={p.keyHint ?? 'API-Key…'}
                    value={keyValues[p.id] ?? ''}
                    onChange={e => setKey(p.id, e.target.value)}
                    style={{ width: 180, fontSize: 12, height: 30 }}
                  />
                  <button
                    className="btn btn--ghost btn--sm"
                    onClick={async () => {
                      try { const t = await navigator.clipboard.readText(); if (t.trim()) setKey(p.id, t.trim()) } catch {}
                    }}
                    title="Aus Zwischenablage"
                  >
                    <Clipboard size={12} />
                  </button>
                  {p.signupUrl && (
                    <a href={p.signupUrl} target="_blank" rel="noopener noreferrer" className="btn btn--ghost btn--sm" title="Registrierung öffnen">
                      <ExternalLink size={12} />
                    </a>
                  )}
                </div>
              )}

              {isCustom && (
                <button className="btn btn--ghost btn--sm" onClick={() => removeCustomProvider(p.id)} title="Entfernen">
                  <Trash2 size={12} />
                </button>
              )}
            </div>
          )
        })}
      </div>
    )
  }

  // ── JSX ──────────────────────────────────────────────────────────────────────
  return (
    <div className="page">
      <div className="page-header">
        <h1 className="page-title" style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
          <Wand2 size={20} /> Setup-Wizard
        </h1>
      </div>

      {/* Step indicator */}
      <div className="wizard-steps">
        {(['select', 'wizard', 'export'] as Step[]).map((s, i) => (
          <div key={s} className={`wizard-step${step === s ? ' wizard-step--active' : ''}${i < stepIndex ? ' wizard-step--done' : ''}`}>
            <div className="wizard-step-num">{i < stepIndex ? <Check size={12} /> : i + 1}</div>
            <span className="wizard-step-label">
              {s === 'select' ? 'Provider' : s === 'wizard' ? 'API-Keys' : 'Export'}
            </span>
          </div>
        ))}
      </div>

      {/* ── Step 1: Provider-Auswahl ─────────────────────────────────────────── */}
      {step === 'select' && (
        <>
          {/* Gateway konfigurieren */}
          <div className="card" style={{ marginBottom: 16 }}>
            <div className="card-header">
              <h2 className="card-title">Gateway</h2>
              <span className="card-count" style={{ fontFamily: 'var(--hw-font-mono)', fontSize: 12 }}>
                {gatewayHost}:{gatewayPort}
              </span>
            </div>
            <div className="card-body">
              <div style={{ display: 'grid', gridTemplateColumns: '1fr 100px', gap: 12, marginBottom: 12 }}>
                <div className="field-group" style={{ marginBottom: 0 }}>
                  <label className="field-label">Host</label>
                  <input className="input" value={gatewayHost} onChange={e => setGatewayHost(e.target.value)} />
                </div>
                <div className="field-group" style={{ marginBottom: 0 }}>
                  <label className="field-label">Port</label>
                  <input className="input" type="number" value={gatewayPort} onChange={e => setGatewayPort(parseInt(e.target.value) || 8765)} min={1} max={65535} />
                </div>
              </div>
              <div className="field-group" style={{ marginBottom: 0 }}>
                <label className="field-label">
                  Bellows API-Key{' '}
                  <span style={{ fontWeight: 400, fontSize: 12, color: 'var(--hw-color-fg-secondary)' }}>(Bearer-Token für deinen Gateway-Endpoint)</span>
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
                    <button type="button" onClick={() => setShowGatewayKey(s => !s)}
                      style={{ position: 'absolute', right: 8, top: '50%', transform: 'translateY(-50%)', background: 'none', border: 'none', cursor: 'pointer', color: 'var(--hw-color-fg-secondary)', display: 'flex', padding: 0 }}
                      aria-label={showGatewayKey ? 'Ausblenden' : 'Anzeigen'}>
                      {showGatewayKey ? <EyeOff size={14} /> : <Eye size={14} />}
                    </button>
                  </div>
                  <button className="btn btn--ghost btn--sm" onClick={() => setGatewayKey(randomKey())} title="Neu generieren">
                    <RefreshCw size={13} />
                  </button>
                </div>
                <span className="field-hint">Modell-Format: <code className="inline-code">bellows/auto</code> (Router) · <code className="inline-code">bellows/gpt-4o</code> (gezielt)</span>
              </div>
            </div>
          </div>

          {/* Provider-Liste mit Checkboxen */}
          <div className="card">
            <div className="card-header">
              <h2 className="card-title">Provider auswählen</h2>
              <span className="card-count">{selectedIds.size} / {allProviders.length} ausgewählt</span>
            </div>

            {/* Toolbar */}
            <div className="provider-toolbar">
              <button className="btn btn--ghost btn--sm" onClick={selectAll}>Alle</button>
              <button className="btn btn--ghost btn--sm" onClick={selectNone}>Keine</button>
              <button className="btn btn--ghost btn--sm" onClick={selectCloud}>Nur Cloud</button>
              <button className="btn btn--ghost btn--sm" onClick={selectLocal}>Nur Lokal</button>
              <div style={{ flex: 1, minWidth: 140, position: 'relative' }}>
                <Search size={13} style={{ position: 'absolute', left: 8, top: '50%', transform: 'translateY(-50%)', color: 'var(--hw-color-fg-secondary)', pointerEvents: 'none' }} />
                <input
                  className="input"
                  placeholder="Suchen…"
                  value={search}
                  onChange={e => setSearch(e.target.value)}
                  style={{ paddingLeft: 28, height: 30, fontSize: 13 }}
                />
              </div>
            </div>

            {/* Scrollable provider list */}
            <div className="provider-scroll">
              {renderProviderGroup('Bevorzugt', preferred)}
              {renderProviderGroup('Cloud', cloud)}
              {renderProviderGroup('Lokal', local)}
              {renderProviderGroup('Eigene', custom)}
              {filtered.length === 0 && (
                <div style={{ padding: '24px 16px', color: 'var(--hw-color-fg-secondary)', fontSize: 13 }}>Keine Provider gefunden.</div>
              )}
            </div>

            {/* Custom provider form */}
            <div style={{ padding: '10px 16px', borderTop: '1px solid var(--hw-color-border)' }}>
              {showCustomForm ? (
                <div style={{ display: 'flex', flexDirection: 'column', gap: 8 }}>
                  <div style={{ display: 'flex', gap: 8, flexWrap: 'wrap' }}>
                    <input className="input" placeholder="Provider-ID (z.B. my-openai)" value={customId} onChange={e => setCustomId(e.target.value)} style={{ flex: '1 1 140px' }} />
                    <input className="input" placeholder="Base-URL (https://… oder http://localhost:…/v1)" value={customUrl} onChange={e => setCustomUrl(e.target.value)} style={{ flex: '2 1 240px' }} />
                    <input className="input" placeholder="Env-Var für API-Key (optional)" value={customEnv} onChange={e => setCustomEnv(e.target.value)} style={{ flex: '1 1 140px' }} />
                  </div>
                  <div style={{ display: 'flex', gap: 6 }}>
                    <button className="btn btn--primary btn--sm" onClick={addCustom} disabled={!customId.trim() || !customUrl.trim()}>Hinzufügen</button>
                    <button className="btn btn--ghost btn--sm" onClick={() => setShowCustomForm(false)}>Abbrechen</button>
                  </div>
                </div>
              ) : (
                <button className="btn btn--ghost btn--sm" onClick={() => setShowCustomForm(true)}>
                  <Plus size={13} /> Eigenen Provider hinzufügen
                </button>
              )}
            </div>

            <div className="card-footer">
              <button
                className="btn btn--primary"
                disabled={selectedIds.size === 0}
                onClick={() => {
                  if (needsKeys.length > 0) { setWizardIndex(0); setStep('wizard') }
                  else setStep('export')
                }}
              >
                {needsKeys.length > 0
                  ? `Key-Wizard starten (${needsKeys.length} ohne Key)`
                  : 'Weiter → Export'
                }
                <ChevronRight size={14} />
              </button>
              {selectedIds.size > 0 && (
                <span style={{ fontSize: 12, color: 'var(--hw-color-fg-secondary)', marginLeft: 8 }}>
                  {wizardProviders.filter(p => keyValues[p.id]).length} / {wizardProviders.length} Cloud-Keys gesetzt
                </span>
              )}
            </div>
          </div>
        </>
      )}

      {/* ── Step 2: Key-Wizard ───────────────────────────────────────────────── */}
      {step === 'wizard' && currentProvider && (
        <div className="card">
          <div className="card-header">
            <h2 className="card-title">{currentProvider.name}</h2>
            <span className="card-count">{wizardIndex + 1} / {wizardProviders.length}</span>
          </div>
          <div className="card-body">
            <p className="card-prose" style={{ marginBottom: 4, fontFamily: 'var(--hw-font-mono)', fontSize: 12 }}>
              {currentProvider.baseUrl}
            </p>

            {currentProvider.signupUrl && (
              <a
                href={currentProvider.signupUrl}
                target="_blank"
                rel="noopener noreferrer"
                className="btn btn--primary"
                style={{ marginBottom: 20, marginTop: 12, display: 'inline-flex', alignItems: 'center', gap: 6, textDecoration: 'none' }}
              >
                <ExternalLink size={14} />
                Registrierung / API-Keys öffnen →
              </a>
            )}

            {/* Clipboard status box */}
            <div className={`clipboard-watch${clipStatus === 'detected' ? ' clipboard-watch--detected' : ''}`}>
              {clipStatus === 'detected' ? (
                <><Check size={20} className="clip-icon clip-icon--ok" /><span>Key erkannt — wird gespeichert…</span></>
              ) : (
                <><Clipboard size={20} className="clip-icon clip-icon--pulse" /><span>Kopiere deinen API-Key — wird automatisch erkannt</span></>
              )}
            </div>

            {/* Fortschritts-Balken für Keys */}
            {wizardProviders.length > 1 && (
              <div className="wizard-progress">
                {wizardProviders.map((p, i) => (
                  <div key={p.id} className={`wizard-progress-dot${i === wizardIndex ? ' wizard-progress-dot--active' : ''}${keyValues[p.id] ? ' wizard-progress-dot--done' : ''}`} title={p.name} />
                ))}
              </div>
            )}

            {/* Manuelle Eingabe */}
            <div className="field-group" style={{ marginTop: 16 }}>
              <label className="field-label" style={{ fontSize: 12 }}>
                Oder manuell eingeben{currentProvider.keyHint && ` (${currentProvider.keyHint})`}:
              </label>
              <div style={{ display: 'flex', gap: 6 }}>
                <input
                  className="input"
                  type="password"
                  placeholder={currentProvider.keyHint ?? 'API-Key einfügen…'}
                  value={keyValues[currentProvider.id] ?? ''}
                  onChange={e => setKey(currentProvider.id, e.target.value)}
                />
                <button
                  className="btn btn--ghost btn--sm"
                  onClick={async () => {
                    try { const t = await navigator.clipboard.readText(); if (t.trim()) setKey(currentProvider.id, t.trim()) } catch {}
                  }}
                  title="Aus Zwischenablage einfügen"
                >
                  <Clipboard size={13} />
                </button>
              </div>
            </div>
          </div>

          <div className="card-footer" style={{ justifyContent: 'space-between' }}>
            <button className="btn btn--ghost btn--sm" onClick={() => {
              clipStopRef.current = true
              setClipStatus('waiting')
              if (wizardIndex > 0) setWizardIndex(i => i - 1)
              else setStep('select')
            }}>
              <ChevronLeft size={14} /> Zurück
            </button>
            <div style={{ display: 'flex', gap: 8 }}>
              <button className="btn btn--ghost btn--sm" onClick={manualAdvance}>Überspringen</button>
              <button
                className="btn btn--primary"
                disabled={!keyValues[currentProvider.id]}
                onClick={manualAdvance}
              >
                {keyValues[currentProvider.id] && <Check size={14} />}
                {wizardIndex + 1 < wizardProviders.length ? 'Weiter' : 'Fertig'}
                <ChevronRight size={14} />
              </button>
            </div>
          </div>
        </div>
      )}

      {/* ── Step 3: Export ──────────────────────────────────── */}
      {step === 'export' && (
        <>
          {/* ── Gateway live actions ─────────────────────────── */}
          <div className="card">
            <div className="card-header">
              <h2 className="card-title">Gateway</h2>
              <span style={{ display: 'flex', alignItems: 'center', gap: 6, fontSize: 13 }}>
                <span className={`gw-dot gw-dot--${gwOnline === null ? 'connecting' : gwOnline ? 'stable' : 'offline'}`} />
                {gwOnline === null ? 'Prüfe…' : gwOnline ? 'Online' : 'Offline'}
              </span>
            </div>
            <div className="card-body">
              {gwOnline === true && (
                <>
                  <p className="card-prose">Gateway läuft — Konfiguration jetzt anwenden, kein Terminal nötig.</p>
                  <div style={{ display: 'flex', gap: 8, flexWrap: 'wrap', marginTop: 8 }}>
                    <button
                      className="btn btn--primary"
                      disabled={applyState === 'busy'}
                      onClick={applyToGateway}
                    >
                      {applyState === 'busy' ? <RefreshCw size={14} className="spin" /> : <Zap size={14} />}
                      {applyState === 'busy' ? 'Wird angewendet…' : 'Konfiguration anwenden'}
                    </button>
                    <button
                      className="btn btn--ghost"
                      disabled={restartState === 'busy' || restartState === 'waiting'}
                      onClick={restartGateway}
                    >
                      <RefreshCw size={14} className={restartState === 'busy' || restartState === 'waiting' ? 'spin' : ''} />
                      {restartState === 'waiting' ? 'Startet neu…' : 'Gateway neu starten'}
                    </button>
                  </div>
                  {applyState === 'ok' && (
                    <div className="apply-status apply-status--ok"><Check size={14} /> Angewendet — {applyMsg}</div>
                  )}
                  {restartState === 'ok' && (
                    <div className="apply-status apply-status--ok"><Check size={14} /> Gateway neu gestartet</div>
                  )}
                  {(applyState === 'err' || restartState === 'err') && (
                    <div className="apply-status apply-status--err">{applyMsg || 'Fehler'}</div>
                  )}
                </>
              )}
              {gwOnline === false && (
                <>
                  <p className="card-prose">Gateway offline — Dateien herunterladen und Script starten:</p>
                  <div style={{ display: 'flex', gap: 8, flexWrap: 'wrap', marginTop: 8 }}>
                    <button className="btn btn--primary" onClick={() => doExport('json')}>
                      <Download size={14} /> bellows.config.json
                    </button>
                    <button className="btn btn--ghost" onClick={() => doExport('bat')}>
                      <Download size={14} /> start-bellows.bat
                      <span style={{ fontSize: 11, marginLeft: 4, color: 'var(--hw-color-fg-secondary)' }}>Windows</span>
                    </button>
                    <button className="btn btn--ghost" onClick={() => doExport('sh')}>
                      <Download size={14} /> start-bellows.sh
                      <span style={{ fontSize: 11, marginLeft: 4, color: 'var(--hw-color-fg-secondary)' }}>Mac/Linux</span>
                    </button>
                  </div>
                  <div style={{ display: 'flex', flexDirection: 'column', gap: 12, marginTop: 14 }}>
                    <div className="step-item">
                      <span className="step-num">1</span>
                      <span>Dateien neben <code className="inline-code">bellows.exe</code> ablegen</span>
                    </div>
                    <div className="step-item">
                      <span className="step-num">2a</span>
                      <div style={{ flex: 1, display: 'flex', flexDirection: 'column', gap: 4 }}>
                        <span><strong>Mac / Linux:</strong></span>
                        <CopyCmd cmd="chmod +x start-bellows.sh && ./start-bellows.sh" />
                      </div>
                    </div>
                    <div className="step-item">
                      <span className="step-num">2b</span>
                      <span><strong>Windows:</strong> Doppelklick auf <code className="inline-code">start-bellows.bat</code></span>
                    </div>
                    <div className="step-item">
                      <span className="step-num">3</span>
                      <button
                        className="btn btn--ghost btn--sm"
                        onClick={() => {
                          setGwOnline(null)
                          bellowsApi.getHealth().then(() => setGwOnline(true)).catch(() => setGwOnline(false))
                        }}
                      >
                        <RefreshCw size={12} /> Verbindung prüfen
                      </button>
                    </div>
                  </div>
                </>
              )}
              {gwOnline === null && (
                <p style={{ color: 'var(--hw-color-fg-secondary)', fontSize: 14 }}>Prüfe Verbindung…</p>
              )}
            </div>
          </div>

          {/* Verbindungs-Info */}
          <div className="card">
            <div className="card-header"><h2 className="card-title">Verbindungs-Info für externe Apps</h2></div>
            <div className="card-body">
              <p className="card-prose" style={{ marginBottom: 12 }}>
                Trage diese Werte in OpenCode, Cursor, Continue.dev oder jede andere OpenAI-kompatible App ein:
              </p>
              <CopyField label="Base URL" value={gatewayUrl} />
              <CopyField label="API Key (Bellows)" value={gatewayKey} secret />
              <CopyField label="Modell Auto-Routing" value="bellows/auto" />
              <CopyField label="Modell Direkt-Routing" value="bellows/<modellname>" />
            </div>
          </div>

          {gwOnline === true && (
            <div className="card">
              <div className="card-header">
                <h2 className="card-title">Dateien herunterladen</h2>
                <span style={{ fontSize: 12, color: 'var(--hw-color-fg-secondary)' }}>Backup / Neuinstallation</span>
              </div>
              <div className="card-body">
                <div style={{ display: 'flex', gap: 8, flexWrap: 'wrap' }}>
                  <button className="btn btn--ghost btn--sm" onClick={() => doExport('json')}><Download size={13} /> bellows.config.json</button>
                  <button className="btn btn--ghost btn--sm" onClick={() => doExport('bat')}><Download size={13} /> start-bellows.bat</button>
                  <button className="btn btn--ghost btn--sm" onClick={() => doExport('sh')}><Download size={13} /> start-bellows.sh</button>
                </div>
              </div>
            </div>
          )}

          <div style={{ display: 'flex', gap: 8, justifyContent: 'space-between', marginTop: 4 }}>
            <button className="btn btn--ghost" onClick={() => { setApplyState('idle'); setRestartState('idle'); setStep('select') }}>
              <ChevronLeft size={14} /> Provider bearbeiten
            </button>
            <button className="btn btn--primary" onClick={() => navigate('/')}>
              <Globe size={14} /> Verbinden
            </button>
          </div>
        </>
      )}
    </div>
  )
}
