import { useState, useEffect, type ReactNode } from 'react'
import { Sidebar } from './Sidebar'

interface AppLayoutProps {
  children: ReactNode
}

const styles: Record<string, React.CSSProperties> = {
  root: {
    display: 'flex',
    height: '100vh',
    overflow: 'hidden',
    backgroundColor: '#f7fafc',
  },
  main: {
    flex: 1,
    overflowY: 'auto',
    display: 'flex',
    flexDirection: 'column',
  },
  hamburger: {
    background: 'none',
    border: 'none',
    fontSize: '1.5rem',
    cursor: 'pointer',
    padding: '1rem',
    alignSelf: 'flex-start',
    color: '#1a202c',
    lineHeight: 1,
  },
  content: {
    padding: '1.5rem',
    flex: 1,
  },
}

export const AppLayout = ({ children }: AppLayoutProps) => {
  const [isMobile, setIsMobile] = useState(window.innerWidth < 768)
  const [isSidebarOpen, setIsSidebarOpen] = useState(false)

  useEffect(() => {
    const handler = () => setIsMobile(window.innerWidth < 768)
    window.addEventListener('resize', handler)
    return () => window.removeEventListener('resize', handler)
  }, [])

  return (
    <div style={styles.root}>
      {!isMobile && (
        <Sidebar isOpen={false} onClose={() => {}} />
      )}
      <main style={styles.main}>
        {isMobile && (
          <button
            style={styles.hamburger}
            onClick={() => setIsSidebarOpen(true)}
            aria-label="Abrir menú"
          >
            Menu
          </button>
        )}
        <div style={styles.content}>{children}</div>
      </main>
      {isMobile && (
        <Sidebar
          isOpen={isSidebarOpen}
          onClose={() => setIsSidebarOpen(false)}
        />
      )}
    </div>
  )
}
