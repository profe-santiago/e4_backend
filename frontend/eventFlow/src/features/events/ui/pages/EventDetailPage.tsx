import { useState } from 'react'
import { useParams, useNavigate, Link } from 'react-router-dom'
import { useEventDetail } from '../hooks/useEventDetail'
import { useTicketTypesByEvent } from '../hooks/useTicketTypesByEvent'
import { useAuthStore } from '@/store/auth.store'
import { t } from '@/shared/config/theme'
import { formatDateLong, formatDateTimeShort, toUtc } from '@/shared/utils/formatDate'

const formatPrice = (price: number, currency = 'USD') =>
  new Intl.NumberFormat('en-US', { style: 'currency', currency }).format(price)

const getTicketSaleStatus = (
  saleStartDate: string | null | undefined,
  saleEndDate: string | null | undefined,
  eventStartDate: string,
) => {
  const now = new Date()
  const effectiveEnd = saleEndDate ? toUtc(saleEndDate) : toUtc(eventStartDate)
  if (saleStartDate && toUtc(saleStartDate) > now) return 'not-started'
  if (effectiveEnd < now) return 'ended'
  return 'open'
}

const StockBar = ({ available, total }: { available: number; total: number }) => {
  const pct = total > 0 ? Math.min(Math.round((available / total) * 100), 100) : 0
  const color = pct > 50 ? t.success : pct > 20 ? t.warning : t.error
  return (
    <div style={{ marginTop: '0.35rem' }}>
      <div style={{ height: '4px', borderRadius: '999px', background: t.border2, overflow: 'hidden' }}>
        <div style={{ height: '100%', width: `${pct}%`, background: color, borderRadius: '999px', transition: 'width 0.4s ease' }} />
      </div>
    </div>
  )
}

export const EventDetailPage = () => {
  const { id } = useParams<{ id: string }>()
  const navigate = useNavigate()
  const isAuthenticated = useAuthStore((s) => s.isAuthenticated)
  const { data: event, isLoading, isError } = useEventDetail(id ?? '')
  const { data: ticketTypes = [] } = useTicketTypesByEvent(id ?? '')
  const [showAuthPrompt, setShowAuthPrompt] = useState(false)

  if (isLoading) return <div style={s.feedback}>Cargando evento...</div>
  if (isError || !event) return <div style={s.error}>No se pudo cargar el evento.</div>

  const isCancelled = event.status === 'CANCELLED'

  return (
    <div style={s.container}>

      {/* ── Hero imagen ── */}
      <div style={s.heroWrap}>
        {event.imageUrl
          ? <img src={event.imageUrl} alt={event.title} style={s.heroImg} />
          : <div style={s.heroPlaceholder}>🎫</div>
        }
        <div style={s.heroOverlay} />

        <button style={s.back} onClick={() => navigate(-1)}>
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round"><path d="M19 12H5"/><path d="m12 19-7-7 7-7"/></svg>
          Volver
        </button>

        {/* título sobre la imagen */}
        <div style={s.heroContent}>
          {event.category?.name && (
            <span style={s.categoryPill}>{event.category.name}</span>
          )}
          <h1 style={s.heroTitle}>{event.title}</h1>
        </div>
      </div>

      {/* ── Info strip ── */}
      <div style={s.infoStrip}>
        <div style={s.infoItem}>
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke={t.accent} strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><rect x="3" y="4" width="18" height="18" rx="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg>
          <div>
            <span style={s.infoLabel}>Fecha</span>
            <span style={s.infoValue}>{formatDateLong(event.startDate)}</span>
          </div>
        </div>
        <div style={s.infoDivider} />
        <div style={s.infoItem}>
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke={t.accent} strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"/><circle cx="12" cy="10" r="3"/></svg>
          <div>
            <span style={s.infoLabel}>Lugar</span>
            <span style={s.infoValue}>{event.venue}, {event.city}</span>
          </div>
        </div>
        <div style={s.infoDivider} />
        <div style={s.infoItem}>
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke={t.accent} strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg>
          <div>
            <span style={s.infoLabel}>Estado del evento</span>
            <span style={{
              ...s.infoValue,
              color: isCancelled ? t.error : event.endDate && toUtc(event.endDate) < new Date() ? t.textDim : t.success,
            }}>
              {isCancelled ? 'Cancelado' : toUtc(event.startDate) > new Date() ? 'Próximamente' : toUtc(event.endDate ?? event.startDate) < new Date() ? 'Finalizado' : 'En curso'}
            </span>
          </div>
        </div>
      </div>

      <div style={s.body}>
        {/* ── Descripción ── */}
        {event.description && (
          <section style={s.section}>
            <h2 style={s.sectionTitle}>Sobre el evento</h2>
            <p style={s.description}>{event.description}</p>
          </section>
        )}

        {/* ── Cancelado ── */}
        {isCancelled && (
          <div style={s.banner(t.error)}>
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>
            Este evento fue cancelado. No es posible adquirir entradas.
          </div>
        )}

        {/* ── Entradas ── */}
        {ticketTypes.length > 0 && !isCancelled && (
          <section style={s.section}>
            <h2 style={s.sectionTitle}>Entradas disponibles</h2>
            <div style={s.ticketsList}>
              {ticketTypes.map((tt) => {
                const saleStatus = getTicketSaleStatus(tt.saleStartDate, tt.saleEndDate, event.startDate)
                const canBuy = tt.availableQuantity > 0 && saleStatus === 'open'
                const soldOut = tt.availableQuantity === 0 && saleStatus === 'open'

                return (
                  <div key={tt.id} style={{ ...s.ticketCard, ...(canBuy ? {} : s.ticketCardDim) }}>
                    <div style={s.ticketLeft}>
                      <p style={s.ticketName}>{tt.name}</p>
                      {tt.description && <p style={s.ticketDesc}>{tt.description}</p>}

                      {/* fechas de venta — siempre visibles */}
                      {(tt.saleStartDate || tt.saleEndDate) && (
                        <div style={s.saleDates}>
                          {tt.saleStartDate && (
                            <span style={s.saleDateItem}>
                              <svg width="11" height="11" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><rect x="3" y="4" width="18" height="18" rx="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg>
                              Inicio venta: {formatDateTimeShort(tt.saleStartDate)}
                            </span>
                          )}
                          {tt.saleEndDate && (
                            <span style={s.saleDateItem}>
                              <svg width="11" height="11" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><rect x="3" y="4" width="18" height="18" rx="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg>
                              Fin venta: {formatDateTimeShort(tt.saleEndDate)}
                            </span>
                          )}
                        </div>
                      )}

                      {/* disponibilidad */}
                      {saleStatus === 'open' && !soldOut && (
                        <>
                          <p style={s.ticketStock}>
                            {tt.availableQuantity} disponibles de {tt.totalQuantity}
                          </p>
                          <StockBar available={tt.availableQuantity} total={tt.totalQuantity} />
                        </>
                      )}
                      {soldOut && <p style={s.ticketSoldOut}>Agotado</p>}
                      {saleStatus === 'ended' && <p style={s.ticketSoldOut}>Venta cerrada</p>}
                    </div>

                    <div style={s.ticketRight}>
                      <p style={s.ticketPrice}>{formatPrice(tt.price, tt.currency)}</p>
                      <button
                        className={canBuy ? 'ef-btn' : 'ef-btn-ghost'}
                        disabled={!canBuy}
                        style={{ fontSize: '0.85rem', padding: '0.5rem 1.1rem', opacity: canBuy ? 1 : 0.5 }}
                        onClick={() => {
                          if (!isAuthenticated) { setShowAuthPrompt(true); return }
                          navigate(`/events/${event.id}/checkout`, { state: { ticketTypeId: tt.id } })
                        }}
                      >
                        {canBuy ? 'Comprar' : saleStatus === 'not-started' ? 'Próximamente' : 'No disponible'}
                      </button>
                    </div>
                  </div>
                )
              })}
            </div>

            {/* Prompt de auth */}
            {showAuthPrompt && (
              <div style={s.authPrompt}>
                <div style={s.authIcon}>
                  <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke={t.accent} strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><rect x="3" y="11" width="18" height="11" rx="2" ry="2"/><path d="M7 11V7a5 5 0 0 1 10 0v4"/></svg>
                </div>
                <div style={s.authBody}>
                  <p style={s.authTitle}>Iniciá sesión para comprar</p>
                  <p style={s.authSub}>Necesitás una cuenta para adquirir entradas y gestionar tus compras.</p>
                  <div style={s.authActions}>
                    <Link to="/login" state={{ from: `/events/${id}` }} className="ef-btn" style={{ textDecoration: 'none', textAlign: 'center', fontSize: '0.875rem' }}>
                      Iniciar sesión
                    </Link>
                    <Link to="/register" state={{ from: `/events/${id}` }} className="ef-btn-ghost" style={{ textDecoration: 'none', textAlign: 'center', fontSize: '0.875rem' }}>
                      Crear cuenta
                    </Link>
                    <button className="ef-btn-ghost" style={{ color: t.textDim, borderColor: 'transparent', fontSize: '0.875rem' }} onClick={() => setShowAuthPrompt(false)}>
                      Cancelar
                    </button>
                  </div>
                </div>
              </div>
            )}
          </section>
        )}
      </div>
    </div>
  )
}

// eslint-disable-next-line @typescript-eslint/no-explicit-any
const s: Record<string, any> = {
  container:      { maxWidth: '860px', margin: '0 auto' },
  feedback:       { textAlign: 'center', padding: '4rem', color: t.textMuted },
  error:          { textAlign: 'center', padding: '4rem', color: t.error },

  heroWrap:       { position: 'relative', height: '380px', borderRadius: '14px', overflow: 'hidden', marginBottom: '1.5rem', background: t.surface2 },
  heroImg:        { width: '100%', height: '100%', objectFit: 'cover', display: 'block' },
  heroPlaceholder:{ width: '100%', height: '100%', display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: '5rem', opacity: 0.2 },
  heroOverlay:    { position: 'absolute', inset: 0, background: 'linear-gradient(to top, rgba(11,23,32,0.9) 0%, rgba(11,23,32,0.3) 50%, transparent 75%)' },
  back:           { position: 'absolute', top: '1rem', left: '1rem', display: 'flex', alignItems: 'center', gap: '0.4rem', background: 'rgba(11,23,32,0.65)', backdropFilter: 'blur(8px)', border: `1px solid ${t.border2}`, borderRadius: '8px', color: t.text, cursor: 'pointer', padding: '0.45rem 0.875rem', fontSize: '0.85rem', fontWeight: 500 },
  heroContent:    { position: 'absolute', bottom: 0, left: 0, right: 0, padding: '1.5rem 1.75rem' },
  categoryPill:   { display: 'inline-block', background: `${t.accent}22`, border: `1px solid ${t.accent}55`, color: t.accent, fontSize: '0.68rem', fontWeight: 700, textTransform: 'uppercase' as const, letterSpacing: '0.09em', padding: '0.2rem 0.65rem', borderRadius: '999px', marginBottom: '0.6rem' },
  heroTitle:      { fontSize: 'clamp(1.4rem, 4vw, 2rem)', fontWeight: 800, color: '#fff', lineHeight: 1.2, margin: 0, textShadow: '0 2px 12px rgba(0,0,0,0.4)' },

  infoStrip:      { display: 'flex', gap: '0', background: t.surface, border: `1px solid ${t.border}`, borderRadius: '12px', padding: '1rem 1.5rem', marginBottom: '1.5rem', flexWrap: 'wrap' as const },
  infoItem:       { display: 'flex', alignItems: 'flex-start', gap: '0.625rem', flex: 1, minWidth: '160px' },
  infoDivider:    { width: '1px', background: t.border, margin: '0 1.25rem', flexShrink: 0 },
  infoLabel:      { display: 'block', fontSize: '0.68rem', fontWeight: 700, textTransform: 'uppercase' as const, letterSpacing: '0.08em', color: t.textDim, marginBottom: '0.2rem' },
  infoValue:      { display: 'block', fontSize: '0.875rem', fontWeight: 600, color: t.text },

  body:           { display: 'flex', flexDirection: 'column', gap: '1.5rem' },
  section:        { display: 'flex', flexDirection: 'column', gap: '0.875rem' },
  sectionTitle:   { fontSize: '1.1rem', fontWeight: 700, color: t.text, margin: 0 },
  description:    { color: t.textMuted, lineHeight: 1.75, fontSize: '0.95rem', margin: 0 },

  banner: (color: string): React.CSSProperties => ({
    display: 'flex', alignItems: 'center', gap: '0.75rem',
    padding: '1rem 1.25rem', background: `${color}12`,
    border: `1px solid ${color}40`, borderRadius: '10px',
    color, fontSize: '0.9rem', fontWeight: 500,
  }),

  ticketsList:    { display: 'flex', flexDirection: 'column', gap: '0.875rem' },
  ticketCard:     { display: 'flex', justifyContent: 'space-between', alignItems: 'center', background: t.surface, border: `1px solid ${t.border}`, borderRadius: '10px', padding: '1.1rem 1.25rem', gap: '1rem' },
  ticketCardDim:  { opacity: 0.7 },
  ticketLeft:     { flex: 1, minWidth: 0 },
  ticketName:     { fontWeight: 600, margin: '0 0 0.2rem', color: t.text, fontSize: '0.975rem' },
  ticketDesc:     { color: t.textMuted, fontSize: '0.82rem', margin: '0 0 0.35rem', lineHeight: 1.4 },
  saleDates:      { display: 'flex', flexDirection: 'column', gap: '0.2rem', margin: '0.4rem 0 0.1rem', padding: '0.45rem 0.65rem', background: t.surface2, borderRadius: '6px', borderLeft: `2px solid ${t.border2}` },
  saleDateItem:   { display: 'flex', alignItems: 'center', gap: '0.35rem', fontSize: '0.75rem', color: t.textMuted },
  ticketStock:    { color: t.success, fontSize: '0.78rem', margin: '0.25rem 0 0', fontWeight: 500 },
  ticketSoldOut:  { color: t.textDim, fontSize: '0.78rem', margin: '0.25rem 0 0' },
  ticketInfo:     { color: t.warning, fontSize: '0.78rem', margin: '0.35rem 0 0', fontWeight: 500 },
  ticketRight:    { display: 'flex', flexDirection: 'column', alignItems: 'flex-end', gap: '0.625rem', flexShrink: 0 },
  ticketPrice:    { fontWeight: 800, fontSize: '1.2rem', margin: 0, color: t.text },

  authPrompt:     { marginTop: '1rem', display: 'flex', gap: '1rem', alignItems: 'flex-start', background: `${t.accent}0F`, border: `1px solid ${t.accent}35`, borderRadius: '12px', padding: '1.25rem 1.5rem' },
  authIcon:       { flexShrink: 0, marginTop: '0.1rem', background: `${t.accent}18`, borderRadius: '8px', padding: '0.5rem', display: 'flex' },
  authBody:       { display: 'flex', flexDirection: 'column', gap: '0.4rem', flex: 1 },
  authTitle:      { margin: 0, fontWeight: 700, fontSize: '0.975rem', color: t.text },
  authSub:        { margin: 0, fontSize: '0.85rem', color: t.textMuted, lineHeight: 1.5 },
  authActions:    { display: 'flex', gap: '0.625rem', marginTop: '0.75rem', flexWrap: 'wrap' as const },
}
