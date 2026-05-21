import { useRouteError } from 'react-router-dom'

function isChunkError(error: unknown): boolean {
  const message = error instanceof Error ? error.message : String(error)
  return (
    message.includes('Failed to fetch dynamically imported module') ||
    message.includes('Importing a module script failed') ||
    message.includes('error loading dynamically imported module')
  )
}

export function RouteErrorElement() {
  const error = useRouteError()

  if (isChunkError(error)) {
    window.location.reload()
    return (
      <div style={styles.container}>
        <p style={styles.message}>Actualizando la aplicación...</p>
        <button style={styles.btn} onClick={() => window.location.reload()}>
          Recargar ahora
        </button>
      </div>
    )
  }

  return (
    <div style={styles.container}>
      <h1 style={styles.title}>Algo salió mal</h1>
      <p style={styles.message}>Ocurrió un error inesperado. Por favor intentá de nuevo.</p>
      <button style={styles.btn} onClick={() => (window.location.href = '/')}>
        Volver al inicio
      </button>
    </div>
  )
}

const styles: Record<string, React.CSSProperties> = {
  container: {
    minHeight: '100vh',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    flexDirection: 'column',
    gap: '1rem',
    background: '#0f1117',
    color: '#e2e8f0',
  },
  title:   { fontSize: '1.5rem', fontWeight: 700, margin: 0 },
  message: { fontSize: '1rem', color: '#94a3b8', margin: 0 },
  btn: {
    padding: '0.5rem 1.25rem',
    borderRadius: '8px',
    background: '#0aada8',
    color: '#fff',
    border: 'none',
    cursor: 'pointer',
    fontSize: '0.9rem',
  },
}
