import { useEvents } from '../hooks/useEvents'
import { useCategories } from '../hooks/useCategories'
import { EventCard } from '../components/EventCard'
import { CategoryFilter } from '../components/CategoryFilter'
import { PaginationControl } from '../components/PaginationControl'
import { t } from '@/shared/config/theme'

const SearchIcon = () => (
  <svg width="17" height="17" viewBox="0 0 24 24" fill="none" stroke={t.textDim} strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" style={{ flexShrink: 0 }}>
    <circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/>
  </svg>
)

const LocationIcon = () => (
  <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
    <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"/><circle cx="12" cy="10" r="3"/>
  </svg>
)

export const EventListPage = () => {
  const {
    data, isLoading, isError,
    page, categoryId,
    searchInput, cityInput,
    onPageChange, onCategoryChange,
    onSearchChange, onCityChange,
    clearFilters, hasFilters,
  } = useEvents()
  const { data: categories = [] } = useCategories()

  return (
    <div>
      {/* ── Hero ── */}
      <div style={s.hero}>
        <div style={s.heroDots} aria-hidden />
        <div style={s.heroInner}>
          <p style={s.eyebrow}>Tu próxima experiencia te espera</p>
          <h1 style={s.heroTitle}>Descubre eventos<br />cerca de ti</h1>
          <p style={s.heroSub}>
            Música, cultura, deporte y más — encuentra las entradas para los eventos que no te puedes perder.
          </p>

          <div style={s.searchWrap}>
            <SearchIcon />
            <input
              type="text"
              placeholder="Buscar eventos..."
              value={searchInput}
              onChange={(e) => onSearchChange(e.target.value)}
              style={s.searchInput}
            />
            <div style={s.searchDivider} />
            <span style={s.searchCity}>
              <LocationIcon />
            </span>
            <input
              type="text"
              placeholder="Ciudad"
              value={cityInput}
              onChange={(e) => onCityChange(e.target.value)}
              style={s.cityInput}
            />
          </div>
        </div>
      </div>

      {/* ── Contenido ── */}
      <div style={s.container}>

        {/* Categorías */}
        <CategoryFilter
          categories={categories}
          selected={categoryId}
          onChange={onCategoryChange}
        />

        {/* Barra de estado */}
        <div style={s.statusBar}>
          {data && data.totalElements > 0
            ? <span style={s.count}>{data.totalElements} evento{data.totalElements !== 1 ? 's' : ''}</span>
            : <span />
          }
          {hasFilters && (
            <button onClick={clearFilters} style={s.clearBtn}>
              Limpiar filtros ✕
            </button>
          )}
        </div>

        {/* Resultados */}
        {isLoading && (
          <div style={s.skeletonGrid}>
            {Array.from({ length: 6 }).map((_, i) => (
              <div key={i} style={s.skeleton} />
            ))}
          </div>
        )}

        {isError && (
          <div style={s.emptyState}>
            <span style={s.emptyIcon}>⚠️</span>
            <p style={s.emptyText}>Error al cargar eventos. Intenta de nuevo.</p>
          </div>
        )}

        {data && (
          <>
            {data.content.length === 0
              ? (
                <div style={s.emptyState}>
                  <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke={t.textDim} strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round">
                    <circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/>
                  </svg>
                  <p style={s.emptyText}>
                    {hasFilters ? 'Sin resultados para esos filtros.' : 'No hay eventos disponibles.'}
                  </p>
                  {hasFilters && (
                    <button onClick={clearFilters} style={s.clearBtn}>Limpiar filtros</button>
                  )}
                </div>
              )
              : (
                <div style={s.grid}>
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

const s: Record<string, React.CSSProperties> = {
  hero: {
    position: 'relative',
    background: `linear-gradient(145deg, #0d1e2e 0%, ${t.surface} 50%, #0a1a26 100%)`,
    borderBottom: `1px solid ${t.border}`,
    padding: '4rem 1.5rem 3rem',
    overflow: 'hidden',
  },
  heroDots: {
    position: 'absolute',
    inset: 0,
    backgroundImage: `radial-gradient(${t.border2} 1px, transparent 1px)`,
    backgroundSize: '28px 28px',
    opacity: 0.35,
    pointerEvents: 'none',
  },
  heroInner: { position: 'relative', maxWidth: '700px', margin: '0 auto' },
  eyebrow: {
    fontSize: '0.73rem', fontWeight: 700, letterSpacing: '0.12em',
    textTransform: 'uppercase' as const, color: t.accent, marginBottom: '0.75rem',
  },
  heroTitle: {
    fontSize: 'clamp(2rem, 5vw, 3rem)', fontWeight: 800,
    color: t.text, lineHeight: 1.1, marginBottom: '1rem',
  },
  heroSub: {
    fontSize: '1rem', color: t.textMuted, lineHeight: 1.7,
    maxWidth: '480px', marginBottom: '2.25rem',
  },
  searchWrap: {
    display: 'flex', alignItems: 'center', gap: '0.75rem',
    background: t.surface2, border: `1px solid ${t.border2}`,
    borderRadius: '12px', padding: '0 1rem',
    maxWidth: '620px', boxShadow: `0 4px 24px rgba(0,0,0,0.2)`,
  },
  searchInput: {
    flex: 2, background: 'none', border: 'none', outline: 'none',
    color: t.text, fontSize: '0.95rem', padding: '0.9rem 0',
  },
  searchDivider: { width: '1px', height: '20px', background: t.border2, flexShrink: 0 },
  searchCity: { color: t.textDim, display: 'flex', alignItems: 'center' },
  cityInput: {
    flex: 1, background: 'none', border: 'none', outline: 'none',
    color: t.text, fontSize: '0.9rem', padding: '0.9rem 0',
  },
  container: { maxWidth: '1140px', margin: '0 auto', padding: '2rem 1.5rem' },
  statusBar: {
    display: 'flex', justifyContent: 'space-between', alignItems: 'center',
    marginBottom: '1.25rem',
  },
  count: { fontSize: '0.82rem', color: t.textDim },
  clearBtn: {
    background: 'none', border: `1px solid ${t.border2}`,
    borderRadius: '8px', color: t.textMuted, fontSize: '0.8rem',
    padding: '0.4rem 0.8rem', cursor: 'pointer',
  },
  grid: {
    display: 'grid',
    gridTemplateColumns: 'repeat(auto-fill, minmax(280px, 1fr))',
    gap: '1.25rem',
  },
  skeletonGrid: {
    display: 'grid',
    gridTemplateColumns: 'repeat(auto-fill, minmax(280px, 1fr))',
    gap: '1.25rem',
  },
  skeleton: {
    height: '320px', borderRadius: '12px',
    background: `linear-gradient(90deg, ${t.surface} 25%, ${t.surface2} 50%, ${t.surface} 75%)`,
    backgroundSize: '200% 100%',
    animation: 'shimmer 1.5s infinite',
  },
  emptyState: {
    display: 'flex', flexDirection: 'column', alignItems: 'center',
    padding: '5rem 1rem', gap: '1rem',
  },
  emptyIcon: { fontSize: '2.5rem' },
  emptyText: { color: t.textMuted, fontSize: '1rem', fontWeight: 500, margin: 0 },
}
