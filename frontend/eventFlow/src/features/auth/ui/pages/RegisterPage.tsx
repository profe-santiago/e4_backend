import { Link } from 'react-router-dom'
import { RegisterForm } from '../components/RegisterForm'
import { t } from '@/shared/config/theme'

export default function RegisterPage() {
  return (
    <div style={styles.container}>
      <div style={styles.glow} />
      <Link to="/" style={styles.back}>← Volver a eventos</Link>
      <div style={styles.card}>
        <div style={styles.brand}>
          <div style={styles.brandIcon}>⬡</div>
          <span style={styles.brandName}>EventFlow</span>
        </div>

        <h1 style={styles.heading}>Crear cuenta</h1>
        <p style={styles.sub}>Completa tus datos para registrarte</p>

        <RegisterForm />

        <p style={styles.footer}>
          ¿Ya tienes cuenta?{' '}
          <Link to="/login" className="ef-link">
            Inicia sesión
          </Link>
        </p>
      </div>
    </div>
  )
}

const styles: Record<string, React.CSSProperties> = {
  container: {
    minHeight: '100vh',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    background: t.base,
    padding: '1.5rem',
    position: 'relative',
    overflow: 'hidden',
  },
  glow: {
    position: 'absolute',
    top: '-20%',
    left: '50%',
    transform: 'translateX(-50%)',
    width: '600px',
    height: '600px',
    background: 'radial-gradient(circle, rgba(10,173,168,0.12) 0%, transparent 70%)',
    pointerEvents: 'none',
  },
  card: {
    position: 'relative',
    width: '100%',
    maxWidth: '480px',
    background: t.surface,
    border: `1px solid ${t.border}`,
    borderRadius: '14px',
    padding: '2.5rem 2rem',
    boxShadow: '0 24px 64px rgba(0,0,0,0.4)',
  },
  brand: {
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    gap: '0.5rem',
    marginBottom: '2rem',
  },
  brandIcon: { fontSize: '1.5rem', color: t.accent, lineHeight: 1 },
  brandName: { fontSize: '1.35rem', fontWeight: 700, color: t.text, letterSpacing: '0.03em' },
  heading: { textAlign: 'center', fontSize: '1.4rem', fontWeight: 700, color: t.text, marginBottom: '0.4rem' },
  sub: { textAlign: 'center', fontSize: '0.9rem', color: t.textMuted, marginBottom: '1.75rem' },
  footer: { textAlign: 'center', marginTop: '1.5rem', fontSize: '0.875rem', color: t.textMuted },
  back: {
    position: 'absolute' as const,
    top: '1.25rem',
    left: '1.5rem',
    textDecoration: 'none',
    color: t.textMuted,
    fontSize: '0.875rem',
    fontWeight: 500,
    zIndex: 1,
  },
}
