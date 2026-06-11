import { useEffect, useState } from 'react'
import { NavLink, Outlet } from 'react-router-dom'
import {
  LayoutDashboard,
  Server,
  Cpu,
  MessageSquare,
  Settings,
  Wind,
  Wand2,
} from 'lucide-react'
import { bellowsApi, type QualityState } from '../api/bellows'

const NAV_ITEMS = [
  { to: '/',           label: 'Overview',   Icon: LayoutDashboard, end: true },
  { to: '/providers',  label: 'Providers',  Icon: Server },
  { to: '/models',     label: 'Models',     Icon: Cpu },
  { to: '/playground', label: 'Playground', Icon: MessageSquare },
]

type GatewayState = QualityState | 'OFFLINE' | 'CONNECTING'

export default function Layout() {
  const [gatewayState, setGatewayState] = useState<GatewayState>('CONNECTING')

  useEffect(() => {
    let active = true
    const poll = async () => {
      try {
        const h = await bellowsApi.getHealth()
        if (active) setGatewayState(h.status)
      } catch {
        if (active) setGatewayState('OFFLINE')
      }
    }
    poll()
    const id = setInterval(poll, 15_000)
    return () => {
      active = false
      clearInterval(id)
    }
  }, [])

  const dotClass = `gw-dot gw-dot--${gatewayState.toLowerCase()}`

  return (
    <div className="layout">
      <aside className="sidebar">
        <div className="sidebar-head">
          <div className="sidebar-brand">
            <Wind size={20} strokeWidth={2.5} className="sidebar-brand-icon" />
            <span className="sidebar-brand-name">Bellows</span>
          </div>
          <span className="sidebar-tagline">LLM Router · Anvil</span>
        </div>

        <nav className="sidebar-nav">
          {NAV_ITEMS.map(({ to, label, Icon, end }) => (
            <NavLink
              key={to}
              to={to}
              end={end}
              className={({ isActive }) =>
                `nav-item${isActive ? ' nav-item--active' : ''}`
              }
            >
              <Icon size={16} />
              <span>{label}</span>
            </NavLink>
          ))}
        </nav>

        <div className="sidebar-foot">
          <NavLink
            to="/setup"
            className={({ isActive }) =>
              `nav-item${isActive ? ' nav-item--active' : ''}`
            }
          >
            <Wand2 size={16} />
            <span>Setup</span>
          </NavLink>

          <NavLink
            to="/config"
            className={({ isActive }) =>
              `nav-item${isActive ? ' nav-item--active' : ''}`
            }
          >
            <Settings size={16} />
            <span>Config</span>
          </NavLink>

          <div className="gw-status">
            <span className={dotClass} />
            <span className="gw-label">
              {gatewayState === 'CONNECTING' ? 'Connecting…' : gatewayState}
            </span>
          </div>
        </div>
      </aside>

      <main className="main">
        <Outlet />
      </main>
    </div>
  )
}
