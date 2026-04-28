import { useState } from 'react'
import { useParams, useLocation, useNavigate } from 'react-router-dom'
import { useEventDetail } from '@/features/events/ui/hooks/useEventDetail'
import { useTicketTypesByEvent } from '@/features/events/ui/hooks/useTicketTypesByEvent'
import { useCreateOrder } from '../hooks/useCreateOrder'

interface LocationState {
  ticketTypeId: number
}

const formatPrice = (price: number) =>
  new Intl.NumberFormat('es-AR', { style: 'currency', currency: 'ARS' }).format(price)

export const CheckoutPage = () => {
  const { id: eventId } = useParams<{ id: string }>()
  const location = useLocation()
  const navigate = useNavigate()
  const state = location.state as LocationState | null

  const [quantity, setQuantity] = useState(1)
  const [paymentMethodId, setPaymentMethodId] = useState('')

  const { data: event, isLoading: isLoadingEvent } = useEventDetail(eventId ?? '')
  const { data: ticketTypes = [], isLoading: isLoadingTickets } = useTicketTypesByEvent(eventId ?? '')
  const { mutate: createOrder, isPending } = useCreateOrder()

  if (isLoadingEvent || isLoadingTickets) return <div style={styles.feedback}>Cargando...</div>
  if (!event || !state?.ticketTypeId) {
    return (
      <div style={styles.feedback}>
        Información incompleta.{' '}
        <button style={styles.link} onClick={() => navigate(-1)}>Volver</button>
      </div>
    )
  }

  const ticketType = ticketTypes.find((tt) => tt.id === state.ticketTypeId)
  if (!ticketType) return <div style={styles.feedback}>Ticket no encontrado.</div>

  const maxQty = Math.min(ticketType.availableQuantity, 10)
  const total = ticketType.price * quantity

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault()
    createOrder({
      items: [{ eventId: event.id, ticketTypeId: ticketType.id, quantity }],
      paymentMethodId,
    })
  }

  return (
    <div style={styles.container}>
      <button style={styles.back} onClick={() => navigate(-1)}>← Volver</button>
      <h1 style={styles.heading}>Checkout</h1>

      <div style={styles.summary}>
        <h2 style={styles.eventTitle}>{event.title}</h2>
        <p style={styles.eventMeta}>{event.venue} — {event.city}</p>

        <div style={styles.ticketInfo}>
          <p style={styles.ticketName}>{ticketType.name}</p>
          <p style={styles.ticketPrice}>{formatPrice(ticketType.price)} por entrada</p>
        </div>
      </div>

      <form onSubmit={handleSubmit} style={styles.form}>
        <div style={styles.field}>
          <label htmlFor="quantity">Cantidad</label>
          <select
            id="quantity"
            value={quantity}
            onChange={(e) => setQuantity(Number(e.target.value))}
            style={styles.input}
          >
            {Array.from({ length: maxQty }, (_, i) => i + 1).map((n) => (
              <option key={n} value={n}>{n}</option>
            ))}
          </select>
        </div>

        <div style={styles.field}>
          <label htmlFor="paymentMethod">ID de método de pago (Stripe)</label>
          <input
            id="paymentMethod"
            type="text"
            placeholder="pm_card_visa"
            value={paymentMethodId}
            onChange={(e) => setPaymentMethodId(e.target.value)}
            style={styles.input}
            required
          />
          <span style={styles.hint}>En desarrollo usar: pm_card_visa</span>
        </div>

        <div style={styles.total}>
          <span>Total a pagar</span>
          <strong>{formatPrice(total)}</strong>
        </div>

        <button type="submit" disabled={isPending} style={styles.submitBtn}>
          {isPending ? 'Procesando...' : `Confirmar compra — ${formatPrice(total)}`}
        </button>
      </form>
    </div>
  )
}

const styles: Record<string, React.CSSProperties> = {
  container: { maxWidth: '560px', margin: '0 auto', padding: '2rem 1rem' },
  feedback: { textAlign: 'center', padding: '4rem', color: '#555' },
  back: { background: 'none', border: 'none', cursor: 'pointer', color: '#3182ce', marginBottom: '1rem', padding: 0, fontSize: '0.9rem' },
  heading: { fontSize: '1.75rem', fontWeight: 700, marginBottom: '1.25rem' },
  summary: { border: '1px solid #e2e8f0', borderRadius: '8px', padding: '1.25rem', marginBottom: '1.5rem', background: '#f7fafc' },
  eventTitle: { fontSize: '1.1rem', fontWeight: 600, margin: '0 0 0.25rem' },
  eventMeta: { color: '#555', fontSize: '0.875rem', margin: '0 0 0.75rem' },
  ticketInfo: { borderTop: '1px solid #e2e8f0', paddingTop: '0.75rem' },
  ticketName: { fontWeight: 500, margin: 0 },
  ticketPrice: { color: '#3182ce', fontWeight: 600, margin: '0.2rem 0 0' },
  form: { display: 'flex', flexDirection: 'column', gap: '1.25rem' },
  field: { display: 'flex', flexDirection: 'column', gap: '0.25rem' },
  input: { padding: '0.5rem', fontSize: '1rem', borderRadius: '4px', border: '1px solid #cbd5e0' },
  hint: { color: '#888', fontSize: '0.8rem' },
  total: { display: 'flex', justifyContent: 'space-between', padding: '0.75rem 0', borderTop: '1px solid #e2e8f0', fontSize: '1rem' },
  submitBtn: { padding: '0.85rem', fontSize: '1rem', background: '#38a169', color: '#fff', border: 'none', borderRadius: '4px', cursor: 'pointer', fontWeight: 600 },
  link: { background: 'none', border: 'none', color: '#3182ce', cursor: 'pointer', textDecoration: 'underline' },
}
