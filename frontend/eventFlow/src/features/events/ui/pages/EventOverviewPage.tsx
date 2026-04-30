import { useParams, useNavigate } from 'react-router-dom'
import { useEventDetail } from '../hooks/useEventDetail'
import { useTicketTypesByEvent } from '../hooks/useTicketTypesByEvent'
import { useEventActions } from '../hooks/useEventActions'
import type { EventStatus } from '../../domain/entities/Event'
import { t } from '@/shared/config/theme'

const STATUS_LABEL: Record<EventStatus, string> = {
  DRAFT:     'Borrador',
  PUBLISHED: 'Publicado',
  CANCELLED: 'Cancelado',
}

const STATUS_COLOR: Record<EventStatus, string> = {
  DRAFT:     '#d69e2e',
  PUBLISHED: '#38a169',
  CANCELLED: '#718096',
}

const fmt = (price: number, currency = 'ARS') =>
  new Intl.NumberFormat('es-AR', { style: 'currency', currency }).format(price)

const fmtDate = (iso: string) =>
  new Date(iso).toLocaleDateString('es-AR', { weekday: 'long', day: '2-digit', month: 'long', year: 'numeric' })

const pct = (sold: number, total: number) =>
  total > 0 ? Math.round((sold / total) * 100) : 0

export const EventOverviewPage = () => {
  const { id } = useParams<{ id: string }>()
  const navigate = useNavigate()
  const { data: event, isLoading, isError } = useEventDetail(id ?? '')
  const { data: ticketTypes = [], isLoading: isLoadingTickets } = useTicketTypesByEvent(id ?? '')
  const { changeStatus } = useEventActions()

  if (isLoading) return <div style={styles.feedback}>Cargando evento...</div>
  if (isError || !event) return <div style={styles.error}>No se pudo cargar el evento.</div>

  const totalCapacity = ticketTypes.reduce((a, tt) => a + tt.totalQuantity, 0)
  const totalSold     = ticketTypes.reduce((a, tt) => a + (tt.totalQuantity - tt.availableQuantity), 0)
  const totalRevenue  = ticketTypes.reduce((a, tt) => a + (tt.totalQuantity - tt.availableQuantity) * tt.price, 0)
  const primaryCurrency = ticketTypes[0]?.currency ?? 'ARS'
  const occupancy = pct(totalSold, totalCapacity)

  return (
    <div style={styles.container}>

      {/* ── Encabezado ── */}
      <div style={styles.header}>
        <button style={styles.back} onClick={() => navigate('/my-events')}>← Mis eventos</button>
        <div style={styles.titleRow}>
          <h1 style={styles.heading}>{event.title}</h1>
          <span style={{ ...styles.badge, background: STATUS_COLOR[event.status] }}>
            {STATUS_LABEL[event.status]}
          </span>
        </div>
        <div style={styles.headerActions}>
          <button className="ef-btn-ghost" style={styles.actionBtn} onClick={() => navigate(`/events/${event.id}/edit`)}>
            Editar evento
          </button>
          {event.status === 'DRAFT' && (
            <button className="ef-btn" style={styles.actionBtn} disabled={changeStatus.isPending}
              onClick={() => changeStatus.mutate({ id: event.id, status: 'PUBLISHED' })}>
              Publicar
            </button>
          )}
          {event.status === 'PUBLISHED' && (
            <button className="ef-btn-ghost" style={{ ...styles.actionBtn, color: t.error, borderColor: t.error }}
              disabled={changeStatus.isPending}
              onClick={() => changeStatus.mutate({ id: event.id, status: 'CANCELLED' })}>
              Cancelar evento
            </button>
          )}
        </div>
      </div>

      <div style={styles.grid}>

        {/* ── Columna izquierda ── */}
        <div style={styles.left}>

          {/* Imagen */}
          {event.imageUrl && (
            <img src={event.imageUrl} alt={event.title} style={styles.image} />
          )}

          {/* Info del evento */}
          <div style={styles.card}>
            <h2 style={styles.cardTitle}>Información del evento</h2>
            <div style={styles.infoList}>
              <InfoRow label="Lugar"   value={event.venue} />
              <InfoRow label="Ciudad"  value={`${event.city}, ${event.country}`} />
              <InfoRow label="Inicio"  value={fmtDate(event.startDate)} />
              {event.endDate && event.endDate !== event.startDate && (
                <InfoRow label="Fin" value={fmtDate(event.endDate)} />
              )}
              {event.category?.name && (
                <InfoRow label="Categoría" value={event.category.name} />
              )}
            </div>
            {event.description && (
              <p style={styles.description}>{event.description}</p>
            )}
          </div>
        </div>

        {/* ── Columna derecha ── */}
        <div style={styles.right}>

          {/* KPIs */}
          <div style={styles.kpiGrid}>
            <KpiCard label="Tickets vendidos" value={totalSold.toString()} sub={`de ${totalCapacity} totales`} />
            <KpiCard label="Ocupación"         value={`${occupancy}%`}       sub={occupancy >= 80 ? '¡Casi lleno!' : occupancy >= 50 ? 'Buen ritmo' : 'En venta'} accent={occupancy >= 80} />
            <KpiCard label="Ingresos estimados" value={fmt(totalRevenue, primaryCurrency)} sub="basado en ventas actuales" wide />
          </div>

          {/* Barra de ocupación global */}
          <div style={styles.card}>
            <div style={styles.barHeader}>
              <span style={styles.cardTitle}>Capacidad total</span>
              <span style={styles.barPct}>{occupancy}%</span>
            </div>
            <div style={styles.barTrack}>
              <div style={{ ...styles.barFill, width: `${occupancy}%`, background: occupancy >= 80 ? t.warning : t.accent }} />
            </div>
            <span style={styles.barSub}>{totalSold} vendidos · {totalCapacity - totalSold} disponibles</span>
          </div>

          {/* Desglose por tipo de ticket */}
          <div style={styles.card}>
            <h2 style={styles.cardTitle}>Por tipo de ticket</h2>
            {isLoadingTickets
              ? <p style={styles.feedbackSm}>Cargando...</p>
              : ticketTypes.length === 0
                ? <p style={styles.feedbackSm}>Sin tipos de ticket configurados.</p>
                : (
                  <div style={styles.ttList}>
                    {ticketTypes.map((tt) => {
                      const sold = tt.totalQuantity - tt.availableQuantity
                      const p    = pct(sold, tt.totalQuantity)
                      return (
                        <div key={tt.id} style={styles.ttRow}>
                          <div style={styles.ttMeta}>
                            <span style={styles.ttName}>{tt.name}</span>
                            <span style={styles.ttPrice}>{fmt(tt.price, tt.currency)}</span>
                          </div>
                          <div style={styles.ttStats}>
                            <div style={styles.ttBarTrack}>
                              <div style={{ ...styles.ttBarFill, width: `${p}%` }} />
                            </div>
                            <span style={styles.ttNumbers}>
                              {sold}/{tt.totalQuantity} · {fmt(sold * tt.price, tt.currency)}
                            </span>
                          </div>
                          <span style={{ ...styles.ttBadge, color: tt.availableQuantity === 0 ? t.error : t.success }}>
                            {tt.availableQuantity === 0 ? 'Agotado' : `${tt.availableQuantity} disp.`}
                          </span>
                        </div>
                      )
                    })}
                  </div>
                )
            }
          </div>

        </div>
      </div>
    </div>
  )
}

// ── Sub-componentes ───────────────────────────────────────────────────────────

const InfoRow = ({ label, value }: { label: string; value: string }) => (
  <div style={infoStyles.row}>
    <span style={infoStyles.label}>{label}</span>
    <span style={infoStyles.value}>{value}</span>
  </div>
)

const KpiCard = ({ label, value, sub, accent, wide }: { label: string; value: string; sub: string; accent?: boolean; wide?: boolean }) => (
  <div style={{ ...kpiStyles.card, ...(wide ? kpiStyles.wide : {}), ...(accent ? kpiStyles.accent : {}) }}>
    <span style={kpiStyles.label}>{label}</span>
    <span style={{ ...kpiStyles.value, ...(accent ? kpiStyles.accentValue : {}) }}>{value}</span>
    <span style={kpiStyles.sub}>{sub}</span>
  </div>
)

// ── Styles ───────────────────────────────────────────────────────────────────

const styles: Record<string, React.CSSProperties> = {
  container:    { maxWidth: '1100px', margin: '0 auto' },
  feedback:     { textAlign: 'center', padding: '4rem', color: t.textMuted },
  feedbackSm:   { color: t.textMuted, fontSize: '0.875rem' },
  error:        { textAlign: 'center', padding: '4rem', color: t.error },
  header:       { marginBottom: '1.75rem' },
  back:         { background: 'none', border: 'none', cursor: 'pointer', color: t.accent, fontSize: '0.9rem', padding: 0, marginBottom: '0.75rem', display: 'block', fontWeight: 500 },
  titleRow:     { display: 'flex', alignItems: 'center', gap: '0.75rem', flexWrap: 'wrap', marginBottom: '0.875rem' },
  heading:      { fontSize: '1.75rem', fontWeight: 700, color: t.text, margin: 0 },
  badge:        { display: 'inline-block', padding: '0.2rem 0.7rem', borderRadius: '9999px', fontSize: '0.8rem', fontWeight: 600, color: '#fff', flexShrink: 0 },
  headerActions:{ display: 'flex', gap: '0.625rem', flexWrap: 'wrap' },
  actionBtn:    { padding: '0.45rem 1rem', fontSize: '0.875rem' },
  grid:         { display: 'grid', gridTemplateColumns: '320px 1fr', gap: '1.25rem', alignItems: 'start' },
  left:         { display: 'flex', flexDirection: 'column', gap: '1rem' },
  right:        { display: 'flex', flexDirection: 'column', gap: '1rem' },
  image:        { width: '100%', height: '200px', objectFit: 'cover', borderRadius: '10px', border: `1px solid ${t.border}` },
  card:         { background: t.surface, border: `1px solid ${t.border}`, borderRadius: '10px', padding: '1.25rem', display: 'flex', flexDirection: 'column', gap: '0.75rem' },
  cardTitle:    { fontSize: '0.85rem', fontWeight: 600, color: t.textMuted, textTransform: 'uppercase' as const, letterSpacing: '0.06em', margin: 0 },
  infoList:     { display: 'flex', flexDirection: 'column', gap: '0.5rem' },
  description:  { color: t.textMuted, fontSize: '0.875rem', lineHeight: 1.6, margin: 0, borderTop: `1px solid ${t.border}`, paddingTop: '0.75rem' },
  kpiGrid:      { display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '0.75rem' },
  barHeader:    { display: 'flex', justifyContent: 'space-between', alignItems: 'center' },
  barPct:       { fontWeight: 700, color: t.text, fontSize: '1rem' },
  barTrack:     { height: '8px', background: t.border, borderRadius: '4px', overflow: 'hidden' },
  barFill:      { height: '100%', borderRadius: '4px', transition: 'width 0.4s ease' },
  barSub:       { fontSize: '0.8rem', color: t.textDim },
  ttList:       { display: 'flex', flexDirection: 'column', gap: '0.875rem' },
  ttRow:        { display: 'grid', gridTemplateColumns: '1fr auto', gap: '0.5rem 1rem', alignItems: 'center' },
  ttMeta:       { display: 'flex', justifyContent: 'space-between', alignItems: 'baseline', gridColumn: '1 / -1' },
  ttName:       { fontWeight: 600, color: t.text, fontSize: '0.9rem' },
  ttPrice:      { color: t.accent, fontWeight: 500, fontSize: '0.875rem' },
  ttStats:      { display: 'flex', flexDirection: 'column', gap: '0.3rem' },
  ttBarTrack:   { height: '5px', background: t.border, borderRadius: '3px', overflow: 'hidden', width: '100%' },
  ttBarFill:    { height: '100%', background: t.accent, borderRadius: '3px', transition: 'width 0.4s ease' },
  ttNumbers:    { fontSize: '0.78rem', color: t.textMuted },
  ttBadge:      { fontSize: '0.75rem', fontWeight: 600, flexShrink: 0, textAlign: 'right' as const },
}

const infoStyles: Record<string, React.CSSProperties> = {
  row:   { display: 'flex', justifyContent: 'space-between', gap: '1rem', fontSize: '0.875rem' },
  label: { color: t.textDim, flexShrink: 0 },
  value: { color: t.text, textAlign: 'right' as const },
}

const kpiStyles: Record<string, React.CSSProperties> = {
  card:        { background: t.surface, border: `1px solid ${t.border}`, borderRadius: '10px', padding: '1rem 1.25rem', display: 'flex', flexDirection: 'column', gap: '0.2rem' },
  wide:        { gridColumn: '1 / -1' },
  accent:      { borderColor: t.accent, background: `${t.accent}10` },
  label:       { fontSize: '0.72rem', color: t.textDim, textTransform: 'uppercase' as const, letterSpacing: '0.07em' },
  value:       { fontSize: '1.5rem', fontWeight: 700, color: t.text, lineHeight: 1.1 },
  accentValue: { color: t.accent },
  sub:         { fontSize: '0.75rem', color: t.textMuted },
}
