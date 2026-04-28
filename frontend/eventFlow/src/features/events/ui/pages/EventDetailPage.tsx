import { useParams, useNavigate } from 'react-router-dom'
import { useEventDetail } from '../hooks/useEventDetail'
import { useTicketTypesByEvent } from '../hooks/useTicketTypesByEvent'

const formatDate = (iso: string) =>
  new Date(iso).toLocaleDateString('es-AR', { weekday: 'long', day: '2-digit', month: 'long', year: 'numeric' })

const formatPrice = (price: number) =>
  new Intl.NumberFormat('es-AR', { style: 'currency', currency: 'ARS' }).format(price)

export const EventDetailPage = () => {
  const { id } = useParams<{ id: string }>()
  const navigate = useNavigate()
  const { data: event, isLoading, isError } = useEventDetail(id ?? '')
  const { data: ticketTypes = [] } = useTicketTypesByEvent(id ?? '')

  if (isLoading) return <div style={styles.feedback}>Cargando evento...</div>
  if (isError || !event) return <div style={styles.error}>No se pudo cargar el evento.</div>

  return (
    <div style={styles.container}>
      <button style={styles.back} onClick={() => navigate(-1)}>← Volver</button>

      {event.imageUrl && (
        <img src={event.imageUrl} alt={event.title} style={styles.image} />
      )}

      <div style={styles.body}>
        <span style={styles.category}>{event.category?.name}</span>
        <h1 style={styles.title}>{event.title}</h1>

        <div style={styles.meta}>
          <span>{event.venue}, {event.city}, {event.country}</span>
          <span>{formatDate(event.startDate)}</span>
          {event.endDate !== event.startDate && <span>hasta {formatDate(event.endDate)}</span>}
        </div>

        <p style={styles.description}>{event.description}</p>

        {ticketTypes.length > 0 && (
          <section style={styles.ticketsSection}>
            <h2 style={styles.ticketsHeading}>Entradas disponibles</h2>
            <div style={styles.ticketsList}>
              {ticketTypes.map((tt) => (
                <div key={tt.id} style={styles.ticketCard}>
                  <div>
                    <p style={styles.ticketName}>{tt.name}</p>
                    {tt.description && <p style={styles.ticketDesc}>{tt.description}</p>}
                    <p style={styles.ticketStock}>
                      {tt.availableQuantity > 0
                        ? `${tt.availableQuantity} disponibles`
                        : 'Agotado'}
                    </p>
                  </div>
                  <div style={styles.ticketRight}>
                    <p style={styles.ticketPrice}>{formatPrice(tt.price)}</p>
                    <button
                      style={{ ...styles.buyBtn, ...(tt.availableQuantity === 0 ? styles.buyBtnDisabled : {}) }}
                      disabled={tt.availableQuantity === 0}
                      onClick={() => navigate(`/events/${event.id}/checkout`, { state: { ticketTypeId: tt.id } })}
                    >
                      {tt.availableQuantity > 0 ? 'Comprar' : 'Agotado'}
                    </button>
                  </div>
                </div>
              ))}
            </div>
          </section>
        )}
      </div>
    </div>
  )
}

const styles: Record<string, React.CSSProperties> = {
  container: { maxWidth: '800px', margin: '0 auto', padding: '2rem 1rem' },
  feedback: { textAlign: 'center', padding: '4rem', color: '#555' },
  error: { textAlign: 'center', padding: '4rem', color: '#e53e3e' },
  back: { background: 'none', border: 'none', cursor: 'pointer', color: '#3182ce', marginBottom: '1rem', padding: 0, fontSize: '0.9rem' },
  image: { width: '100%', maxHeight: '360px', objectFit: 'cover', borderRadius: '8px', marginBottom: '1.5rem' },
  body: { display: 'flex', flexDirection: 'column', gap: '0.75rem' },
  category: { fontSize: '0.75rem', color: '#3182ce', fontWeight: 600, textTransform: 'uppercase' },
  title: { fontSize: '2rem', fontWeight: 700, margin: 0 },
  meta: { display: 'flex', flexDirection: 'column', gap: '0.25rem', color: '#555', fontSize: '0.9rem' },
  description: { color: '#333', lineHeight: 1.6, marginTop: '0.5rem' },
  ticketsSection: { marginTop: '1.5rem' },
  ticketsHeading: { fontSize: '1.25rem', fontWeight: 600, marginBottom: '1rem' },
  ticketsList: { display: 'flex', flexDirection: 'column', gap: '0.75rem' },
  ticketCard: { display: 'flex', justifyContent: 'space-between', alignItems: 'center', border: '1px solid #e2e8f0', borderRadius: '8px', padding: '1rem' },
  ticketName: { fontWeight: 600, margin: 0 },
  ticketDesc: { color: '#555', fontSize: '0.85rem', margin: '0.2rem 0 0' },
  ticketStock: { color: '#888', fontSize: '0.8rem', margin: '0.2rem 0 0' },
  ticketRight: { display: 'flex', flexDirection: 'column', alignItems: 'flex-end', gap: '0.5rem' },
  ticketPrice: { fontWeight: 700, fontSize: '1.1rem', margin: 0 },
  buyBtn: { padding: '0.5rem 1.25rem', background: '#3182ce', color: '#fff', border: 'none', borderRadius: '4px', cursor: 'pointer', fontWeight: 600 },
  buyBtnDisabled: { background: '#cbd5e0', cursor: 'not-allowed' },
}
