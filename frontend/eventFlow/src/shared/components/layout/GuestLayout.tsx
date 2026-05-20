import type { ReactNode } from 'react'
import { Link } from 'react-router-dom'
import { t } from '@/shared/config/theme'
import { Footer } from './Footer'

interface Props {
  children: ReactNode
}

export const GuestLayout = ({ children }: Props) => (
  <div style={styles.root}>
    <header style={styles.navbar}>
      <Link to="/" style={styles.logo}>
        <span style={styles.logoIcon}>⬡</span>
        <span style={styles.logoText}>EventFlow</span>
      </Link>
      <div style={styles.actions}>
        <Link to="/login" style={styles.loginBtn}>Iniciar sesión</Link>
        <Link to="/register" style={styles.registerBtn}>Registrarse</Link>
      </div>
    </header>
    <main style={styles.main}>{children}</main>
    <Footer />
  </div>
)

const styles: Record<string, React.CSSProperties> = {
  root: {
    minHeight: '100vh',
    backgroundColor: t.base,
    display: 'flex',
    flexDirection: 'column',
  },
  navbar: {
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'space-between',
    padding: '0 2rem',
    height: '60px',
    backgroundColor: t.surface,
    borderBottom: `1px solid ${t.border}`,
    position: 'sticky',
    top: 0,
    zIndex: 100,
  },
  logo: {
    display: 'flex',
    alignItems: 'center',
    gap: '0.5rem',
    textDecoration: 'none',
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
  actions: {
    display: 'flex',
    alignItems: 'center',
    gap: '0.75rem',
  },
  loginBtn: {
    textDecoration: 'none',
    color: t.textMuted,
    fontSize: '0.875rem',
    fontWeight: 500,
    padding: '0.4rem 0.875rem',
    borderRadius: '6px',
    border: `1px solid ${t.border2}`,
    transition: 'color 0.15s, border-color 0.15s',
  },
  registerBtn: {
    textDecoration: 'none',
    color: '#fff',
    fontSize: '0.875rem',
    fontWeight: 600,
    padding: '0.4rem 1rem',
    borderRadius: '6px',
    backgroundColor: t.accent,
  },
  main: {
    flex: 1,
    overflowY: 'auto',
  },
}
