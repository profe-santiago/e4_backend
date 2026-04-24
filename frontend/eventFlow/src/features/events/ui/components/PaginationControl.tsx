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
  btn: { padding: '0.5rem 1rem', borderRadius: '4px', border: '1px solid #cbd5e0', background: '#fff', cursor: 'pointer' },
  info: { color: '#555', fontSize: '0.875rem' },
}
