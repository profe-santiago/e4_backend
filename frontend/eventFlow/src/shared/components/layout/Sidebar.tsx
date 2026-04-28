import { useNavigate } from 'react-router-dom'
import { useAuthStore } from '@/store/auth.store'
import { NAV_ITEMS } from '@/shared/config/navigation'
import { SidebarNavItem } from './SidebarNavItem'

interface SidebarProps {
  isOpen: boolean
  onClose: () => void
}

const styles: Record<string, React.CSSProperties> = {
  overlay: {
    position: 'fixed',
    inset: 0,
    backgroundColor: 'rgba(0,0,0,0.5)',
    zIndex: 40,
  },
  sidebar: {
    width: '240px',
    minWidth: '240px',
    height: '100vh',
    backgroundColor: '#1a202c',
    display: 'flex',
    flexDirection: 'column',
    zIndex: 50,
    overflowY: 'auto',
  },
  sidebarMobileOpen: {
    position: 'fixed',
    top: 0,
    left: 0,
  },
  sidebarMobileClosed: {
    position: 'fixed',
    top: 0,
    left: '-240px',
  },
  logo: {
    padding: '1.25rem 1rem',
    fontSize: '1.25rem',
    fontWeight: 700,
    color: '#e2e8f0',
    letterSpacing: '0.05em',
  },
  separator: {
    height: '1px',
    backgroundColor: 'rgba(255,255,255,0.1)',
    margin: '0 1rem',
  },
  nav: {
    flex: 1,
    display: 'flex',
    flexDirection: 'column',
    gap: '0.125rem',
    padding: '0.75rem 0',
  },
  adminLabel: {
    padding: '0.75rem 1rem 0.25rem',
    fontSize: '0.65rem',
    fontWeight: 600,
    color: '#718096',
    textTransform: 'uppercase' as const,
    letterSpacing: '0.08em',
  },
  footer: {
    borderTop: '1px solid rgba(255,255,255,0.1)',
    padding: '1rem',
    display: 'flex',
    flexDirection: 'column',
    gap: '0.5rem',
  },
  email: {
    fontSize: '0.8rem',
    color: '#a0aec0',
    overflow: 'hidden',
    textOverflow: 'ellipsis',
    whiteSpace: 'nowrap' as const,
  },
  logoutButton: {
    background: 'none',
    border: 'none',
    color: '#fc8181',
    cursor: 'pointer',
    textAlign: 'left' as const,
    padding: '0.25rem 0',
    fontSize: '0.875rem',
    fontWeight: 500,
  },
}

export const Sidebar = ({ isOpen, onClose }: SidebarProps) => {
  const navigate = useNavigate()
  const user = useAuthStore((s) => s.user)
  const isAdmin = useAuthStore((s) => s.isAdmin)
  const logout = useAuthStore((s) => s.logout)

  const isMobile = window.innerWidth < 768

  const handleLogout = () => {
    logout()
    navigate('/login')
  }

  const handleNavClick = () => {
    if (isMobile) onClose()
  }

  const publicItems = NAV_ITEMS.filter((item) => !item.adminOnly)
  const adminItems = NAV_ITEMS.filter((item) => item.adminOnly)

  const sidebarPositionStyle = isMobile
    ? isOpen
      ? styles.sidebarMobileOpen
      : styles.sidebarMobileClosed
    : {}

  return (
    <>
      {isMobile && isOpen && (
        <div style={styles.overlay} onClick={onClose} />
      )}
      <aside style={{ ...styles.sidebar, ...sidebarPositionStyle }}>
        <div style={styles.logo}>EventFlow</div>
        <div style={styles.separator} />
        <nav style={styles.nav} onClick={handleNavClick}>
          {publicItems.map((item) => (
            <SidebarNavItem key={item.path} item={item} />
          ))}
          {isAdmin && (
            <>
              <div style={styles.adminLabel}>Admin</div>
              {adminItems.map((item) => (
                <SidebarNavItem key={item.path} item={item} />
              ))}
            </>
          )}
        </nav>
        <div style={styles.footer}>
          <span style={styles.email}>{user?.email}</span>
          <button style={styles.logoutButton} onClick={handleLogout}>
            Cerrar sesión
          </button>
        </div>
      </aside>
    </>
  )
}
