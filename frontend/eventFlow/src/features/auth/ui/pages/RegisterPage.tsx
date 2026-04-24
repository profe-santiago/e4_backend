import { Link } from 'react-router-dom'
import { RegisterForm } from '../components/RegisterForm'

export default function RegisterPage() {
  return (
    <div style={styles.container}>
      <div style={styles.card}>
        <h1 style={styles.title}>EventFlow</h1>
        <h2 style={styles.subtitle}>Crear cuenta</h2>
        <RegisterForm />
        <p style={styles.footer}>
          ¿Ya tenés cuenta?{' '}
          <Link to="/login" style={styles.link}>
            Iniciá sesión
          </Link>
        </p>
      </div>
    </div>
  )
}

const styles: Record<string, React.CSSProperties> = {
  container: { minHeight: '100vh', display: 'flex', alignItems: 'center', justifyContent: 'center', background: '#f7fafc' },
  card: { background: '#fff', padding: '2rem', borderRadius: '8px', boxShadow: '0 1px 4px rgba(0,0,0,0.1)', width: '100%', maxWidth: '480px' },
  title: { textAlign: 'center', marginBottom: '0.25rem', fontSize: '1.5rem' },
  subtitle: { textAlign: 'center', marginBottom: '1.5rem', fontWeight: 400, color: '#555', fontSize: '1rem' },
  footer: { textAlign: 'center', marginTop: '1rem', color: '#555' },
  link: { color: '#3182ce' },
}
