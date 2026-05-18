import { t } from '@/shared/config/theme'

interface Props {
  currentPage: number
  totalPages: number
  onPageChange: (page: number) => void
}

export const PaginationControl = ({ currentPage, totalPages, onPageChange }: Props) => {
  if (totalPages <= 1) return null

  return (
    <div style={styles.container}>
      <button
        className="ef-btn-ghost"
        style={styles.btn}
        disabled={currentPage === 0}
        onClick={() => onPageChange(currentPage - 1)}
      >
        ← Anterior
      </button>
      <span style={styles.info}>
        {currentPage + 1} / {totalPages}
      </span>
      <button
        className="ef-btn-ghost"
        style={styles.btn}
        disabled={currentPage === totalPages - 1}
        onClick={() => onPageChange(currentPage + 1)}
      >
        Siguiente →
      </button>
    </div>
  )
}

const styles: Record<string, React.CSSProperties> = {
  container: { display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '1rem', marginTop: '2rem' },
  btn: { padding: '0.45rem 1rem', fontSize: '0.875rem' },
  info: { color: t.textMuted, fontSize: '0.875rem' },
}
