import { useEffect, useRef, useState } from 'react'
import { Send, RefreshCw, Lock, Globe, Trash2 } from 'lucide-react'
import { bellowsApi, type ChatMessage } from '../api/bellows'

interface Turn {
  role: 'user' | 'assistant'
  content: string
  modelUsed?: string
  usage?: { prompt_tokens: number; completion_tokens: number; total_tokens: number }
  latencyMs?: number
  error?: string
}

export default function Playground() {
  const [turns, setTurns]             = useState<Turn[]>([])
  const [input, setInput]             = useState('')
  const [models, setModels]           = useState<string[]>([])
  const [model, setModel]             = useState('')
  const [privacy, setPrivacy]         = useState<'OPEN' | 'LOCAL_ONLY'>('OPEN')
  const [temperature, setTemperature] = useState(0.7)
  const [maxTokens, setMaxTokens]     = useState(1024)
  const [busy, setBusy]               = useState(false)

  const bottomRef = useRef<HTMLDivElement>(null)

  useEffect(() => {
    bellowsApi.getModels().then(m => setModels(m.data.map(d => d.id))).catch(() => {})
  }, [])

  useEffect(() => {
    bottomRef.current?.scrollIntoView({ behavior: 'smooth' })
  }, [turns, busy])

  const send = async () => {
    if (!input.trim() || busy) return
    const text = input.trim()
    setInput('')
    setTurns(prev => [...prev, { role: 'user', content: text }])
    setBusy(true)
    const t0 = performance.now()

    try {
      const history: ChatMessage[] = turns
        .filter(t => !t.error)
        .map(t => ({ role: t.role, content: t.content }))
      history.push({ role: 'user', content: text })

      const res = await bellowsApi.chat(
        { model: model || undefined, messages: history, temperature, max_tokens: maxTokens },
        privacy,
      )

      setTurns(prev => [
        ...prev,
        {
          role: 'assistant',
          content: res.choices?.[0]?.message?.content ?? '',
          modelUsed: res.model,
          usage: res.usage,
          latencyMs: Math.round(performance.now() - t0),
        },
      ])
    } catch (e) {
      setTurns(prev => [
        ...prev,
        { role: 'assistant', content: '', error: e instanceof Error ? e.message : 'Request failed' },
      ])
    } finally {
      setBusy(false)
    }
  }

  const onKey = (e: React.KeyboardEvent<HTMLTextAreaElement>) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault()
      send()
    }
  }

  return (
    <div className="playground-page">
      {/* Left: controls */}
      <aside className="pg-sidebar">
        <div className="pg-sidebar-inner">
          <h1 className="page-title" style={{ marginBottom: 20 }}>Playground</h1>

          <div className="field-group">
            <label className="field-label">Model</label>
            <select className="select" value={model} onChange={e => setModel(e.target.value)}>
              <option value="">Auto — router decides</option>
              {models.map(m => (
                <option key={m} value={m}>{m}</option>
              ))}
            </select>
          </div>

          <div className="field-group">
            <label className="field-label">Privacy Mode</label>
            <div className="toggle-pair">
              <button
                className={`toggle-btn${privacy === 'OPEN' ? ' toggle-btn--on' : ''}`}
                onClick={() => setPrivacy('OPEN')}
              >
                <Globe size={12} /> OPEN
              </button>
              <button
                className={`toggle-btn toggle-btn--local${privacy === 'LOCAL_ONLY' ? ' toggle-btn--on toggle-btn--local-on' : ''}`}
                onClick={() => setPrivacy('LOCAL_ONLY')}
              >
                <Lock size={12} /> LOCAL ONLY
              </button>
            </div>
            {privacy === 'LOCAL_ONLY' && (
              <p className="field-hint" style={{ color: 'var(--hw-color-success)' }}>
                Only local adapters will be used. Cloud fallback is disabled.
              </p>
            )}
          </div>

          <div className="field-group">
            <label className="field-label">
              Temperature
              <span style={{ float: 'right', color: 'var(--hw-color-fg-secondary)', fontWeight: 400 }}>
                {temperature.toFixed(1)}
              </span>
            </label>
            <input
              type="range" min="0" max="2" step="0.1"
              value={temperature}
              onChange={e => setTemperature(parseFloat(e.target.value))}
              style={{ width: '100%', accentColor: 'var(--hw-color-accent)' }}
            />
          </div>

          <div className="field-group">
            <label className="field-label">Max Tokens</label>
            <input
              type="number" className="input" min="1" max="32768"
              value={maxTokens}
              onChange={e => setMaxTokens(parseInt(e.target.value) || 1024)}
            />
          </div>
        </div>

        <button
          className="btn btn--ghost btn--sm"
          style={{ margin: '0 16px 16px', width: 'calc(100% - 32px)' }}
          onClick={() => setTurns([])}
          disabled={turns.length === 0 || busy}
        >
          <Trash2 size={13} /> Clear conversation
        </button>
      </aside>

      {/* Right: chat */}
      <div className="pg-chat">
        <div className="pg-messages">
          {turns.length === 0 ? (
            <div className="empty-state" style={{ height: '100%' }}>
              <p>Send a message to test the Bellows router</p>
              <small>Shift+Enter for a newline, Enter to send</small>
            </div>
          ) : (
            turns.map((t, i) => (
              <div key={i} className={`turn turn--${t.role}`}>
                <div className="turn-bubble">
                  {t.error
                    ? <span style={{ color: 'var(--hw-color-error)' }}>Error: {t.error}</span>
                    : t.content
                  }
                </div>
                {t.role === 'assistant' && !t.error && (t.modelUsed || t.usage || t.latencyMs) && (
                  <div className="turn-meta">
                    {t.modelUsed && <span>{t.modelUsed}</span>}
                    {t.usage && (
                      <span>
                        {t.usage.prompt_tokens}+{t.usage.completion_tokens}={t.usage.total_tokens} tok
                      </span>
                    )}
                    {t.latencyMs && <span>{t.latencyMs} ms</span>}
                  </div>
                )}
              </div>
            ))
          )}

          {busy && (
            <div className="turn turn--assistant">
              <div className="turn-bubble turn-bubble--thinking">
                <span className="thinking-dot" />
                <span className="thinking-dot" />
                <span className="thinking-dot" />
              </div>
            </div>
          )}
          <div ref={bottomRef} />
        </div>

        <div className="pg-input-bar">
          <textarea
            className="pg-textarea"
            placeholder="Type a message… (Enter to send, Shift+Enter for newline)"
            value={input}
            onChange={e => setInput(e.target.value)}
            onKeyDown={onKey}
            rows={2}
          />
          <button className="btn btn--primary pg-send" onClick={send} disabled={busy || !input.trim()} aria-label={busy ? 'Sending message' : 'Send message'}>
            {busy ? <RefreshCw size={16} className="spin" /> : <Send size={16} />}
          </button>
        </div>
      </div>
    </div>
  )
}
