import { NavLink } from 'react-router-dom'
import type { NavItem } from '@/shared/config/navigation'

interface SidebarNavItemProps {
  item: NavItem
}

export const SidebarNavItem = ({ item }: SidebarNavItemProps) => (
  <NavLink
    to={item.path}
    end={item.path === '/'}
    className={({ isActive }) => `ef-nav-item${isActive ? ' active' : ''}`}
  >
    {item.label}
  </NavLink>
)
