import { useState, useEffect } from 'react'
import { useAuthStore } from '@/store/auth.store'
import { useLogout } from '@/features/auth/ui/hooks/useLogout'
import { NAV_ITEMS } from '@/shared/config/navigation'
import { SidebarNavItem } from './SidebarNavItem'
import { t } from '@/shared/config/theme'
import { useUnreadNotificationCount } from '@/features/notifications/ui/hooks/useUnreadNotificationCount'

interface SidebarProps {
  isOpen: boolean
  onClose: () => void
}

export const Sidebar = ({ isOpen, onClose }: SidebarProps) => {
  const user = useAuthStore((s) => s.user)
  const isAdmin = useAuthStore((s) => s.isAdmin)
  const { handleLogout } = useLogout()

  const [isMobile, setIsMobile] = useState(window.innerWidth < 768)

  useEffect(() => {
    const handleResize = () => setIsMobile(window.innerWidth < 768)
    window.addEventListener('resize', handleResize)
    return () => window.removeEventListener('resize', handleResize)
  }, [])

  const handleNavClick = () => {
    if (isMobile) onClose()
  }

  const unreadCount = useUnreadNotificationCount()

  const publicItems = NAV_ITEMS.filter((item) => !item.adminOnly)
  const adminItems = NAV_ITEMS.filter((item) => item.adminOnly)

  const sidebarPositionStyle: React.CSSProperties = isMobile
    ? isOpen
      ? { position: 'fixed', top: 0, left: 0 }
      : { position: 'fixed', top: 0, left: '-240px' }
    : {}

  return (
    <>
      {isMobile && isOpen && (
        <div style={styles.overlay} onClick={onClose} />
      )}
      <aside style={{ ...styles.sidebar, ...sidebarPositionStyle }}>
        <div style={styles.logoArea}>
          <span style={styles.logoIcon}>⬡</span>
          <span style={styles.logoText}>EventFlow</span>
        </div>

        <div style={styles.separator} />

        <nav style={styles.nav} onClick={handleNavClick}>
          {publicItems.map((item) => (
            <SidebarNavItem
              key={item.path}
              item={item}
              badge={item.path === '/notifications' ? unreadCount : undefined}
            />
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
          <div style={styles.userInfo}>
            <div style={styles.avatar}>
              {user?.email?.charAt(0).toUpperCase()}
            </div>
            <div style={styles.userDetails}>
              <span style={styles.userEmail}>{user?.email}</span>
              <span style={styles.userRole}>{user?.role === 'ADMIN' ? 'Administrador' : 'Comprador'}</span>
            </div>
          </div>
          <button style={styles.logoutBtn} onClick={handleLogout}>
            Cerrar sesión
          </button>
        </div>
      </aside>
    </>
  )
}

const styles: Record<string, React.CSSProperties> = {
  overlay: {
    position: 'fixed',
    inset: 0,
    backgroundColor: 'rgba(0,0,0,0.6)',
    zIndex: 40,
    backdropFilter: 'blur(2px)',
  },
  sidebar: {
    width: '240px',
    minWidth: '240px',
    height: '100vh',
    backgroundColor: t.surface,
    borderRight: `1px solid ${t.border}`,
    display: 'flex',
    flexDirection: 'column',
    zIndex: 50,
    overflowY: 'auto',
    transition: 'left 0.25s ease',
  },
  logoArea: {
    display: 'flex',
    alignItems: 'center',
    gap: '0.5rem',
    padding: '1.25rem 1rem',
  },
  logoIcon: {
    fontSize: '1.3rem',
    color: t.accent,
    lineHeight: 1,
  },
  logoText: {
    fontSize: '1.1rem',
    fontWeight: 700,
    color: t.text,
    letterSpacing: '0.04em',
  },
  separator: {
    height: '1px',
    backgroundColor: t.border,
    margin: '0 1rem',
  },
  nav: {
    flex: 1,
    display: 'flex',
    flexDirection: 'column',
    padding: '0.75rem 0',
  },
  adminLabel: {
    padding: '0.85rem 1.5rem 0.3rem',
    fontSize: '0.65rem',
    fontWeight: 600,
    color: t.textDim,
    textTransform: 'uppercase' as const,
    letterSpacing: '0.1em',
  },
  footer: {
    borderTop: `1px solid ${t.border}`,
    padding: '0.875rem 1rem',
    display: 'flex',
    flexDirection: 'column',
    gap: '0.75rem',
  },
  userInfo: {
    display: 'flex',
    alignItems: 'center',
    gap: '0.625rem',
  },
  avatar: {
    width: '32px',
    height: '32px',
    borderRadius: '50%',
    background: `linear-gradient(135deg, ${t.accent}, #088B87)`,
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    fontSize: '0.85rem',
    fontWeight: 700,
    color: '#fff',
    flexShrink: 0,
  },
  userDetails: {
    display: 'flex',
    flexDirection: 'column',
    overflow: 'hidden',
  },
  userEmail: {
    fontSize: '0.78rem',
    color: t.text,
    overflow: 'hidden',
    textOverflow: 'ellipsis',
    whiteSpace: 'nowrap' as const,
    fontWeight: 500,
  },
  userRole: {
    fontSize: '0.7rem',
    color: t.textDim,
  },
  logoutBtn: {
    background: 'none',
    border: `1px solid ${t.border2}`,
    borderRadius: '6px',
    color: t.textMuted,
    cursor: 'pointer',
    padding: '0.45rem 0.75rem',
    fontSize: '0.8rem',
    fontWeight: 500,
    width: '100%',
    textAlign: 'left' as const,
    transition: 'color 0.15s, border-color 0.15s',
  },
}
