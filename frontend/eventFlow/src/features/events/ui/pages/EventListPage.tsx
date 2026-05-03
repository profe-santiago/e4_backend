import { useEvents } from '../hooks/useEvents'
import { useCategories } from '../hooks/useCategories'
import { EventCard } from '../components/EventCard'
import { PaginationControl } from '../components/PaginationControl'
import { t } from '@/shared/config/theme'

export const EventListPage = () => {
  const {
    data, isLoading, isError,
    page, categoryId,
    searchInput, cityInput, venueInput,
    onPageChange, onCategoryChange,
    onSearchChange, onCityChange, onVenueChange,
    clearFilters, hasFilters,
  } = useEvents()
  const { data: categories = [] } = useCategories()

  return (
    <div>
      <div style={styles.hero}>
        <div style={styles.heroInner}>
          <p style={styles.heroEyebrow}>Tu próxima experiencia te espera</p>
          <h1 style={styles.heroTitle}>Descubre eventos cerca de ti</h1>
          <p style={styles.heroSub}>
            Música, cultura, deporte y más — encuentra las entradas para los eventos que no te puedes perder.
          </p>

          <div style={styles.searchBar}>
            <input
              type="text"
              placeholder="Buscar eventos..."
              value={searchInput}
              onChange={(e) => onSearchChange(e.target.value)}
              style={styles.searchInput}
            />
          </div>
        </div>
      </div>

      <div style={styles.container}>
        <div style={styles.filtersRow}>
          <div style={styles.filters}>
            <select
              value={categoryId ?? ''}
              onChange={(e) => onCategoryChange(e.target.value ? Number(e.target.value) : undefined)}
              style={styles.filterSelect}
            >
              <option value="">Todas las categorías</option>
              {categories.map((cat) => (
                <option key={cat.id} value={cat.id}>{cat.name}</option>
              ))}
            </select>

            <input
              type="text"
              placeholder="Ciudad"
              value={cityInput}
              onChange={(e) => onCityChange(e.target.value)}
              style={styles.filterInput}
            />

            <input
              type="text"
              placeholder="Lugar"
              value={venueInput}
              onChange={(e) => onVenueChange(e.target.value)}
              style={styles.filterInput}
            />

            {hasFilters && (
              <button onClick={clearFilters} style={styles.clearBtn}>
                Limpiar filtros ✕
              </button>
            )}
          </div>

          {data && data.totalElements > 0 && (
            <span style={styles.count}>{data.totalElements} evento{data.totalElements !== 1 ? 's' : ''}</span>
          )}
        </div>

        {isLoading && <p style={styles.feedback}>Cargando eventos...</p>}
        {isError && <p style={styles.error}>Error al cargar eventos. Intenta de nuevo.</p>}

        {data && (
          <>
            {data.content.length === 0
              ? (
                <div style={styles.emptyState}>
                  <span style={styles.emptyIcon}>🔍</span>
                  <p style={styles.emptyText}>
                    {hasFilters ? 'No se encontraron eventos con esos filtros.' : 'No hay eventos disponibles en este momento.'}
                  </p>
                  {hasFilters && (
                    <button onClick={clearFilters} style={styles.clearBtn}>
                      Limpiar filtros
                    </button>
                  )}
                </div>
              )
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
    </div>
  )
}

const styles: Record<string, React.CSSProperties> = {
  hero: {
    background: `linear-gradient(135deg, ${t.surface} 0%, #0d1e2e 60%, ${t.base} 100%)`,
    borderBottom: `1px solid ${t.border}`,
    padding: '3.5rem 1.5rem 2.5rem',
  },
  heroInner: {
    maxWidth: '1100px',
    margin: '0 auto',
  },
  heroEyebrow: {
    fontSize: '0.78rem',
    fontWeight: 700,
    letterSpacing: '0.1em',
    textTransform: 'uppercase' as const,
    color: t.accent,
    marginBottom: '0.75rem',
  },
  heroTitle: {
    fontSize: 'clamp(1.8rem, 4vw, 2.75rem)',
    fontWeight: 800,
    color: t.text,
    lineHeight: 1.15,
    marginBottom: '0.875rem',
  },
  heroSub: {
    fontSize: '1rem',
    color: t.textMuted,
    lineHeight: 1.65,
    maxWidth: '520px',
    marginBottom: '2rem',
  },
  searchBar: {
    display: 'flex',
    alignItems: 'center',
    background: t.surface2,
    border: `1px solid ${t.border2}`,
    borderRadius: '10px',
    padding: '0 1rem',
    maxWidth: '560px',
    gap: '0.625rem',
  },
  searchInput: {
    flex: 1,
    background: 'none',
    border: 'none',
    outline: 'none',
    color: t.text,
    fontSize: '0.95rem',
    padding: '0.85rem 0',
  },
  container: {
    maxWidth: '1100px',
    margin: '0 auto',
    padding: '2rem 1.5rem',
  },
  filtersRow: {
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'space-between',
    marginBottom: '1.5rem',
    gap: '1rem',
    flexWrap: 'wrap' as const,
  },
  filters: {
    display: 'flex',
    alignItems: 'center',
    gap: '0.625rem',
    flexWrap: 'wrap' as const,
    flex: 1,
  },
  filterSelect: {
    background: t.surface2,
    border: `1px solid ${t.border}`,
    borderRadius: '8px',
    color: t.text,
    fontSize: '0.85rem',
    padding: '0.5rem 0.75rem',
    outline: 'none',
    cursor: 'pointer',
  },
  filterInput: {
    background: t.surface2,
    border: `1px solid ${t.border}`,
    borderRadius: '8px',
    color: t.text,
    fontSize: '0.85rem',
    padding: '0.5rem 0.75rem',
    outline: 'none',
    width: '130px',
  },
  clearBtn: {
    background: 'none',
    border: `1px solid ${t.border2}`,
    borderRadius: '8px',
    color: t.textMuted,
    fontSize: '0.8rem',
    padding: '0.5rem 0.75rem',
    cursor: 'pointer',
    whiteSpace: 'nowrap' as const,
  },
  count: {
    fontSize: '0.82rem',
    color: t.textDim,
    whiteSpace: 'nowrap' as const,
  },
  grid: {
    display: 'grid',
    gridTemplateColumns: 'repeat(auto-fill, minmax(270px, 1fr))',
    gap: '1.25rem',
  },
  feedback:  { textAlign: 'center', color: t.textMuted, marginTop: '3rem' },
  error:     { textAlign: 'center', color: t.error, marginTop: '3rem' },
  emptyState: {
    display: 'flex',
    flexDirection: 'column',
    alignItems: 'center',
    padding: '4rem 1rem',
    gap: '0.75rem',
  },
  emptyIcon: { fontSize: '2.5rem' },
  emptyText: { color: t.textMuted, fontSize: '1rem', fontWeight: 500 },
}
