import { useEvents } from '../hooks/useEvents'
import { useCategories } from '../hooks/useCategories'
import { EventCard } from '../components/EventCard'
import { CategoryFilter } from '../components/CategoryFilter'
import { PaginationControl } from '../components/PaginationControl'
import { t } from '@/shared/config/theme'

export const EventListPage = () => {
  const { data, isLoading, isError, page, categoryId, onPageChange, onCategoryChange } = useEvents()
  const { data: categories = [] } = useCategories()

  return (
    <div style={styles.container}>
      <header style={styles.header}>
        <h1 style={styles.heading}>Eventos</h1>
        <p style={styles.sub}>Descubrí los próximos eventos disponibles</p>
      </header>

      <CategoryFilter
        categories={categories}
        selected={categoryId}
        onChange={onCategoryChange}
      />

      {isLoading && <p style={styles.feedback}>Cargando eventos...</p>}
      {isError && <p style={styles.error}>Error al cargar eventos. Intentá de nuevo.</p>}

      {data && (
        <>
          {data.content.length === 0
            ? <p style={styles.feedback}>No hay eventos disponibles.</p>
            : (
              <div style={styles.grid}>
                {data.content.map((event) => (
                  <EventCard key={event.id} event={event} />
                ))}
              </div>
            )
          }
          <PaginationControl
            currentPage={page}
            totalPages={data.totalPages}
            onPageChange={onPageChange}
          />
        </>
      )}
    </div>
  )
}

const styles: Record<string, React.CSSProperties> = {
  container: { maxWidth: '1100px', margin: '0 auto' },
  header:    { marginBottom: '1.75rem' },
  heading:   { fontSize: '1.75rem', fontWeight: 700, color: t.text, marginBottom: '0.25rem' },
  sub:       { fontSize: '0.9rem', color: t.textMuted },
  grid:      { display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(260px, 1fr))', gap: '1.25rem' },
  feedback:  { textAlign: 'center', color: t.textMuted, marginTop: '3rem' },
  error:     { textAlign: 'center', color: t.error, marginTop: '3rem' },
}
