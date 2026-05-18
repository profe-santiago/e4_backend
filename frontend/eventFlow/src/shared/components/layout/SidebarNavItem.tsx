import { NavLink } from 'react-router-dom'
import type { NavItem } from '@/shared/config/navigation'

interface SidebarNavItemProps {
  item: NavItem
  badge?: number
}

export const SidebarNavItem = ({ item, badge }: SidebarNavItemProps) => (
  <NavLink
    to={item.path}
    end={item.path === '/'}
    className={({ isActive }) => `ef-nav-item${isActive ? ' active' : ''}`}
    style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}
  >
    <span>{item.label}</span>
    {badge != null && badge > 0 && (
      <span style={{
        background: '#e53e3e',
        color: '#fff',
        fontSize: '0.65rem',
        fontWeight: 700,
        borderRadius: '999px',
        padding: '0.1rem 0.45rem',
        minWidth: '18px',
        textAlign: 'center',
        lineHeight: '1.4',
      }}>
        {badge > 99 ? '99+' : badge}
      </span>
    )}
  </NavLink>
)
