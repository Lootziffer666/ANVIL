import { useState } from 'react'
import { Save, Eye, EyeOff, CheckCircle } from 'lucide-react'

export default function Config() {
  const [url, setUrl]           = useState(() => localStorage.getItem('bellows_gateway_url') ?? 'http://localhost:8765')
  const [key, setKey]           = useState(() => localStorage.getItem('bellows_gateway_key') ?? '')
  const [showKey, setShowKey]   = useState(false)
  const [saved, setSaved]       = useState(false)

  const save = () => {
    localStorage.setItem('bellows_gateway_url', url.trim() || 'http://localhost:8765')
    if (key.trim()) {
      localStorage.setItem('bellows_gateway_key', key.trim())
    } else {
      localStorage.removeItem('bellows_gateway_key')
    }
    setSaved(true)
    setTimeout(() => setSaved(false), 2500)
  }

  return (
    <div className="page">
      <div className="page-header">
        <h1 className="page-title">Config</h1>
      </div>

      {/* Connection card */}
      <div className="card" style={{ maxWidth: 560 }}>
        <div className="card-header">
          <h2 className="card-title">Gateway Connection</h2>
        </div>
        <div className="card-body">
          <div className="field-group">
            <label className="field-label">Gateway URL</label>
            <input
              className="input"
              type="url"
              value={url}
              onChange={e => setUrl(e.target.value)}
              placeholder="http://localhost:8765"
            />
            <span className="field-hint">Where your Bellows Gateway is running</span>
          </div>

          <div className="field-group">
            <label className="field-label">
              Bearer Key{' '}
              <span style={{ fontWeight: 400, color: 'var(--hw-color-fg-secondary)' }}>
                (optional)
              </span>
            </label>
            <div style={{ position: 'relative' }}>
              <input
                className="input"
                type={showKey ? 'text' : 'password'}
                value={key}
                onChange={e => setKey(e.target.value)}
                placeholder="Leave empty if gateway has no auth"
                style={{ paddingRight: 42 }}
              />
              <button
                onClick={() => setShowKey(s => !s)}
                style={{
                  position: 'absolute', right: 10, top: '50%',
                  transform: 'translateY(-50%)',
                  background: 'none', border: 'none', cursor: 'pointer',
                  color: 'var(--hw-color-fg-secondary)', display: 'flex', padding: 0,
                }}
                type="button"
                aria-label={showKey ? 'Hide key' : 'Show key'}
              >
                {showKey ? <EyeOff size={16} /> : <Eye size={16} />}
              </button>
            </div>
            <span className="field-hint">Stored in localStorage — never sent to third parties</span>
          </div>

          <button className="btn btn--primary" onClick={save} style={{ alignSelf: 'flex-start' }}>
            {saved ? <CheckCircle size={15} /> : <Save size={15} />}
            {saved ? 'Saved!' : 'Save'}
          </button>
        </div>
      </div>

      {/* Quick start */}
      <div className="card" style={{ maxWidth: 560 }}>
        <div className="card-header">
          <h2 className="card-title">Quick Start</h2>
        </div>
        <div className="card-body">
          <p className="card-prose">Start the gateway on your machine:</p>
          <pre className="codeblock">bellows serve --host 127.0.0.1 --port 8765</pre>

          <p className="card-prose">Initialise a config file:</p>
          <pre className="codeblock">bellows config init</pre>

          <p className="card-prose">Store an API key (never in plaintext config):</p>
          <pre className="codeblock">bellows key set openai-key</pre>

          <p className="card-prose">List available models:</p>
          <pre className="codeblock">bellows models</pre>
        </div>
      </div>

      {/* Privacy note */}
      <div className="card" style={{ maxWidth: 560 }}>
        <div className="card-header">
          <h2 className="card-title">Privacy Contract</h2>
        </div>
        <div className="card-body">
          <p className="card-prose">
            When <code className="inline-code">PrivacyMode.LOCAL_ONLY</code> is set (via the{' '}
            <code className="inline-code">X-Anvil-Privacy: local_only</code> header or the
            Playground toggle), the router <strong>never</strong> contacts cloud adapters — even
            as a fallback. If no local model is available,{' '}
            <code className="inline-code">BellowsExhaustedException</code> is thrown.
          </p>
          <p className="card-prose">
            API keys are stored in an encrypted JCEKS keystore on the gateway host. This GUI
            only reads from localStorage; no credentials are transmitted to third-party services.
          </p>
        </div>
      </div>
    </div>
  )
}
