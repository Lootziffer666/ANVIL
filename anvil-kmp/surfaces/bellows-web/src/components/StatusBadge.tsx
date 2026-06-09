import type { QualityState } from '../api/bellows'

interface Props {
  state: QualityState | 'OFFLINE' | string
  size?: 'sm' | 'md'
}

const palette: Record<string, { bg: string; dot: string; text: string }> = {
  STABLE:   { bg: 'var(--hw-color-success-light)', dot: 'var(--hw-color-success)',  text: '#15803d' },
  DEGRADED: { bg: 'var(--hw-color-warning-light)', dot: 'var(--hw-color-warning)',  text: '#92400e' },
  BLOCKED:  { bg: 'var(--hw-color-info-light)',    dot: 'var(--hw-color-info)',     text: '#1e40af' },
  FAILED:   { bg: 'var(--hw-color-error-light)',   dot: 'var(--hw-color-error)',    text: '#b91c1c' },
  OFFLINE:  { bg: '#f1f5f9',                        dot: '#94a3b8',                  text: '#64748b' },
}

export default function StatusBadge({ state, size = 'md' }: Props) {
  const c = palette[state] ?? palette['OFFLINE']
  const pad = size === 'sm' ? '2px 7px' : '4px 10px'
  const fs  = size === 'sm' ? '11px' : '12px'
  const ds  = size === 'sm' ? '5px' : '6px'

  return (
    <span
      style={{
        display: 'inline-flex',
        alignItems: 'center',
        gap: 5,
        padding: pad,
        borderRadius: 'var(--hw-radius-sm)',
        background: c.bg,
        color: c.text,
        fontSize: fs,
        fontWeight: 700,
        fontFamily: 'var(--hw-font-body)',
        letterSpacing: '0.04em',
        whiteSpace: 'nowrap',
      }}
    >
      <span
        style={{
          width: ds,
          height: ds,
          borderRadius: '50%',
          background: c.dot,
          flexShrink: 0,
        }}
      />
      {state}
    </span>
  )
}
