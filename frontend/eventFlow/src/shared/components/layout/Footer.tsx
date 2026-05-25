import { t } from '@/shared/config/theme'

export const Footer = () => (
  <footer style={styles.footer}>
    <span style={styles.brand}>⬡ EventFlow</span>
    <span style={styles.sep}>·</span>
    <span style={styles.text}>México</span>
    <span style={styles.sep}>·</span>
    <span style={styles.text}>
      Contacto:{' '}
      <a href="mailto:eeventflow@gmail.com" style={styles.link}>
        eeventflow@gmail.com
      </a>
    </span>
    <span style={styles.sep}>·</span>
    <span style={styles.text}>© {new Date().getFullYear()} EventFlow. Todos los derechos reservados.</span>
  </footer>
)

const styles: Record<string, React.CSSProperties> = {
  footer: {
    display: 'flex',
    flexWrap: 'wrap',
    alignItems: 'center',
    justifyContent: 'center',
    gap: '0.35rem',
    padding: '1.25rem 2rem',
    borderTop: `1px solid ${t.border}`,
    marginTop: 'auto',
  },
  brand: {
    fontWeight: 700,
    fontSize: '0.8rem',
    color: t.accent,
    letterSpacing: '0.03em',
  },
  sep: {
    color: t.textDim,
    fontSize: '0.75rem',
  },
  text: {
    fontSize: '0.78rem',
    color: t.textMuted,
  },
  link: {
    color: t.accent,
    textDecoration: 'none',
    fontSize: '0.78rem',
  },
}
