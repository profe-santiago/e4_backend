import { NavLink } from 'react-router-dom'
import type { NavItem } from '@/shared/config/navigation'

interface SidebarNavItemProps {
  item: NavItem
}

const activeStyle: React.CSSProperties = {
  display: 'flex',
  alignItems: 'center',
  gap: '0.75rem',
  padding: '0.6rem 1rem',
  textDecoration: 'none',
  color: '#e2e8f0',
  backgroundColor: 'rgba(255,255,255,0.1)',
  borderLeft: '3px solid #63b3ed',
  borderRadius: '0 4px 4px 0',
  fontWeight: 500,
}

const inactiveStyle: React.CSSProperties = {
  display: 'flex',
  alignItems: 'center',
  gap: '0.75rem',
  padding: '0.6rem 1rem',
  textDecoration: 'none',
  color: '#a0aec0',
  borderLeft: '3px solid transparent',
  borderRadius: '0 4px 4px 0',
}

export const SidebarNavItem = ({ item }: SidebarNavItemProps) => {
  return (
    <NavLink
      to={item.path}
      end={item.path === '/'}
      style={({ isActive }) => (isActive ? activeStyle : inactiveStyle)}
    >
      <span>{item.label}</span>
    </NavLink>
  )
}
