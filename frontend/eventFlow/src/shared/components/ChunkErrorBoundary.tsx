import { Component, type ReactNode } from 'react'

interface Props {
  children: ReactNode
}

interface State {
  hasError: boolean
  reloading: boolean
}

export class ChunkErrorBoundary extends Component<Props, State> {
  state: State = { hasError: false, reloading: false }

  static getDerivedStateFromError(error: unknown): Partial<State> {
    const message = error instanceof Error ? error.message : String(error)
    const isChunkError =
      message.includes('Failed to fetch dynamically imported module') ||
      message.includes('Importing a module script failed') ||
      message.includes('error loading dynamically imported module')
    if (isChunkError) return { hasError: true }
    return {}
  }

  componentDidCatch(error: unknown) {
    const message = error instanceof Error ? error.message : String(error)
    const isChunkError =
      message.includes('Failed to fetch dynamically imported module') ||
      message.includes('Importing a module script failed') ||
      message.includes('error loading dynamically imported module')
    if (isChunkError && !this.state.reloading) {
      this.setState({ reloading: true })
      window.location.reload()
    }
  }

  render() {
    if (this.state.hasError) {
      return (
        <div style={{ minHeight: '100vh', display: 'flex', alignItems: 'center', justifyContent: 'center', flexDirection: 'column', gap: '1rem', background: '#0f1117', color: '#e2e8f0' }}>
          <p style={{ fontSize: '1rem' }}>Actualizando la aplicación...</p>
          <button
            onClick={() => window.location.reload()}
            style={{ padding: '0.5rem 1.25rem', borderRadius: '8px', background: '#0aada8', color: '#fff', border: 'none', cursor: 'pointer', fontSize: '0.9rem' }}
          >
            Recargar ahora
          </button>
        </div>
      )
    }
    return this.props.children
  }
}
