import { useState } from 'react'
import { useParams, useNavigate, Link } from 'react-router-dom'
import { useEventDetail } from '../hooks/useEventDetail'
import { useTicketTypesByEvent } from '../hooks/useTicketTypesByEvent'
import { useAuthStore } from '@/store/auth.store'
import { t } from '@/shared/config/theme'

const formatDate = (iso: string) =>
  new Date(iso).toLocaleDateString('es-AR', { weekday: 'long', day: '2-digit', month: 'long', year: 'numeric' })

const formatPrice = (price: number, currency = 'USD') =>
  new Intl.NumberFormat('en-US', { style: 'currency', currency }).format(price)

const getTicketSaleStatus = (
  saleStartDate: string | null | undefined,
  saleEndDate: string | null | undefined,
  eventStartDate: string,
) => {
  const now        = new Date()
  const effectiveEnd = saleEndDate ? new Date(saleEndDate) : new Date(eventStartDate)

  if (saleStartDate && new Date(saleStartDate) > now) return 'not-started'
  if (effectiveEnd < now) return 'ended'
  return 'open'
}

export const EventDetailPage = () => {
  const { id } = useParams<{ id: string }>()
  const navigate = useNavigate()
  const isAuthenticated = useAuthStore((s) => s.isAuthenticated)
  const { data: event, isLoading, isError } = useEventDetail(id ?? '')
  const { data: ticketTypes = [] } = useTicketTypesByEvent(id ?? '')
  const [showAuthPrompt, setShowAuthPrompt] = useState(false)

  if (isLoading) return <div style={styles.feedback}>Cargando evento...</div>
  if (isError || !event) return <div style={styles.error}>No se pudo cargar el evento.</div>


  return (
    <div style={styles.container}>
      <button style={styles.back} onClick={() => navigate(-1)}>← Volver</button>

      {event.imageUrl && (
        <img src={event.imageUrl} alt={event.title} style={styles.image} />
      )}

      <div style={styles.body}>
        {event.category?.name && (
          <span style={styles.category}>{event.category.name}</span>
        )}
        <h1 style={styles.title}>{event.title}</h1>

        <div style={styles.meta}>
          <span>📍 {event.venue}, {event.city}, {event.country}</span>
          <span>🗓 {formatDate(event.startDate)}</span>
          {event.endDate !== event.startDate && (
            <span>hasta {formatDate(event.endDate)}</span>
          )}
        </div>

        {event.description && (
          <p style={styles.description}>{event.description}</p>
        )}

        {event.status === 'CANCELLED' && (
          <div style={styles.banner(t.error)}>
            Este evento fue cancelado. No es posible adquirir entradas.
          </div>
        )}

        {ticketTypes.length > 0 && event.status !== 'CANCELLED' && (
          <section style={styles.ticketsSection}>
            <h2 style={styles.ticketsHeading}>Entradas disponibles</h2>
            <div style={styles.ticketsList}>
              {ticketTypes.map((tt) => {
                const saleStatus = getTicketSaleStatus(tt.saleStartDate, tt.saleEndDate, event.startDate)
                const canBuy = tt.availableQuantity > 0 && saleStatus === 'open'
                return (
                  <div key={tt.id} style={styles.ticketCard}>
                    <div>
                      <p style={styles.ticketName}>{tt.name}</p>
                      {tt.description && <p style={styles.ticketDesc}>{tt.description}</p>}
                      {saleStatus === 'not-started' && (
                        <p style={styles.ticketSaleInfo}>
                          Venta desde {new Date(tt.saleStartDate!).toLocaleString('es-AR', { day: '2-digit', month: 'short', hour: '2-digit', minute: '2-digit' })}
                        </p>
                      )}
                      {saleStatus === 'ended' && (
                        <p style={styles.ticketSoldOut}>Venta cerrada</p>
                      )}
                      {saleStatus === 'open' && (
                        <>
                          <p style={canBuy ? styles.ticketStock : styles.ticketSoldOut}>
                            {canBuy ? `${tt.availableQuantity} disponibles` : 'Agotado'}
                          </p>
                          {tt.saleEndDate && (
                            <p style={styles.ticketSaleInfo}>
                              Venta hasta {new Date(tt.saleEndDate).toLocaleString('es-AR', { day: '2-digit', month: 'short', hour: '2-digit', minute: '2-digit' })}
                            </p>
                          )}
                        </>
                      )}
                    </div>
                    <div style={styles.ticketRight}>
                      <p style={styles.ticketPrice}>{formatPrice(tt.price, tt.currency)}</p>
                      <button
                        className="ef-btn"
                        disabled={!canBuy}
                        onClick={() => {
                          if (!isAuthenticated) {
                            setShowAuthPrompt(true)
                            return
                          }
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

            {showAuthPrompt && (
              <div style={styles.authPrompt}>
                <div style={styles.authPromptIcon}>🔒</div>
                <div style={styles.authPromptBody}>
                  <p style={styles.authPromptTitle}>Iniciá sesión para comprar</p>
                  <p style={styles.authPromptSub}>
                    Necesitás una cuenta para adquirir entradas y gestionar tus compras.
                  </p>
                  <div style={styles.authPromptActions}>
                    <Link to="/login" state={{ from: `/events/${id}` }} className="ef-btn" style={{ textDecoration: 'none', textAlign: 'center' }}>
                      Iniciar sesión
                    </Link>
                    <Link to="/register" state={{ from: `/events/${id}` }} className="ef-btn-ghost" style={{ textDecoration: 'none', textAlign: 'center' }}>
                      Crear cuenta
                    </Link>
                    <button className="ef-btn-ghost" style={{ color: t.textDim, borderColor: 'transparent' }} onClick={() => setShowAuthPrompt(false)}>
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

const styles: Record<string, React.CSSProperties> = {
  container:      { maxWidth: '800px', margin: '0 auto' },
  feedback:       { textAlign: 'center', padding: '4rem', color: t.textMuted },
  error:          { textAlign: 'center', padding: '4rem', color: t.error },
  back:           { background: 'none', border: 'none', cursor: 'pointer', color: t.accent, marginBottom: '1.25rem', padding: 0, fontSize: '0.9rem', fontWeight: 500 },
  image:          { width: '100%', maxHeight: '360px', objectFit: 'cover', borderRadius: '10px', marginBottom: '1.75rem' },
  body:           { display: 'flex', flexDirection: 'column', gap: '0.875rem' },
  category:       { fontSize: '0.72rem', color: t.accent, fontWeight: 700, textTransform: 'uppercase', letterSpacing: '0.08em' },
  title:          { fontSize: '2rem', fontWeight: 700, color: t.text, lineHeight: 1.2 },
  meta:           { display: 'flex', flexDirection: 'column', gap: '0.3rem', color: t.textMuted, fontSize: '0.9rem' },
  description:    { color: t.textMuted, lineHeight: 1.65, marginTop: '0.25rem' },
  ticketsSection: { marginTop: '1rem' },
  ticketsHeading: { fontSize: '1.2rem', fontWeight: 600, color: t.text, marginBottom: '1rem' },
  ticketsList:    { display: 'flex', flexDirection: 'column', gap: '0.75rem' },
  ticketCard:     { display: 'flex', justifyContent: 'space-between', alignItems: 'center', background: t.surface, border: `1px solid ${t.border}`, borderRadius: '8px', padding: '1rem 1.25rem' },
  ticketName:     { fontWeight: 600, margin: 0, color: t.text },
  ticketDesc:     { color: t.textMuted, fontSize: '0.85rem', margin: '0.2rem 0 0' },
  ticketStock:    { color: t.success, fontSize: '0.8rem', margin: '0.2rem 0 0', fontWeight: 500 },
  ticketSoldOut:  { color: t.textDim, fontSize: '0.8rem', margin: '0.2rem 0 0' },
  ticketRight:    { display: 'flex', flexDirection: 'column', alignItems: 'flex-end', gap: '0.625rem', flexShrink: 0 },
  ticketPrice:    { fontWeight: 700, fontSize: '1.1rem', margin: 0, color: t.text },
  ticketSaleInfo: { color: t.warning, fontSize: '0.8rem', margin: '0.2rem 0 0', fontWeight: 500 },
  banner: (color: string): React.CSSProperties => ({
    padding: '0.875rem 1.25rem',
    background: `${color}14`,
    border: `1px solid ${color}40`,
    borderRadius: '8px',
    color,
    fontSize: '0.9rem',
    fontWeight: 500,
  }),
  authPrompt: {
    marginTop: '1.25rem',
    display: 'flex',
    gap: '1rem',
    alignItems: 'flex-start',
    background: `${t.accent}0F`,
    border: `1px solid ${t.accent}35`,
    borderRadius: '10px',
    padding: '1.25rem 1.5rem',
  } as React.CSSProperties,
  authPromptIcon: {
    fontSize: '1.5rem',
    flexShrink: 0,
    marginTop: '0.1rem',
  } as React.CSSProperties,
  authPromptBody: {
    display: 'flex',
    flexDirection: 'column',
    gap: '0.4rem',
    flex: 1,
  } as React.CSSProperties,
  authPromptTitle: {
    margin: 0,
    fontWeight: 700,
    fontSize: '0.975rem',
    color: t.text,
  } as React.CSSProperties,
  authPromptSub: {
    margin: 0,
    fontSize: '0.85rem',
    color: t.textMuted,
    lineHeight: 1.5,
  } as React.CSSProperties,
  authPromptActions: {
    display: 'flex',
    gap: '0.625rem',
    marginTop: '0.75rem',
    flexWrap: 'wrap' as const,
  } as React.CSSProperties,
}
