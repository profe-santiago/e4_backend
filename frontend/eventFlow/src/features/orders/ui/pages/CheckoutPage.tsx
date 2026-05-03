import { useState } from 'react'
import { useParams, useLocation, useNavigate } from 'react-router-dom'
import { useEventDetail } from '@/features/events/ui/hooks/useEventDetail'
import { useTicketTypesByEvent } from '@/features/events/ui/hooks/useTicketTypesByEvent'
import { useCreateOrder } from '../hooks/useCreateOrder'
import { t } from '@/shared/config/theme'

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
  const [submitted, setSubmitted] = useState(false)

  const { data: event, isLoading: isLoadingEvent } = useEventDetail(eventId ?? '')
  const { data: ticketTypes = [], isLoading: isLoadingTickets } = useTicketTypesByEvent(eventId ?? '')
  const { mutate: createOrder, isPending } = useCreateOrder()

  if (isLoadingEvent || isLoadingTickets) return <div style={styles.feedback}>Cargando...</div>
  if (!event || !state?.ticketTypeId) {
    return (
      <div style={styles.feedback}>
        Información incompleta.{' '}
        <button style={styles.linkBtn} onClick={() => navigate(-1)}>Volver</button>
      </div>
    )
  }

  const ticketType = ticketTypes.find((tt) => tt.id === state.ticketTypeId)
  if (!ticketType) return <div style={styles.feedback}>Ticket no encontrado.</div>

  const maxQty = Math.min(ticketType.availableQuantity, 10)
  const total = ticketType.price * quantity

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault()
    if (submitted || isPending) return
    setSubmitted(true)
    createOrder({ items: [{ eventId: event.id, ticketTypeId: ticketType.id, quantity }], paymentMethodId })
  }

  return (
    <div style={styles.container}>
      <button style={styles.back} onClick={() => navigate(-1)}>← Volver</button>
      <h1 style={styles.heading}>Checkout</h1>

      <div style={styles.summary}>
        <h2 style={styles.eventTitle}>{event.title}</h2>
        <p style={styles.eventMeta}>📍 {event.venue} — {event.city}</p>
        <div style={styles.divider} />
        <p style={styles.ticketName}>{ticketType.name}</p>
        <p style={styles.ticketPrice}>{formatPrice(ticketType.price)} por entrada</p>
      </div>

      <form onSubmit={handleSubmit} style={styles.form}>
        <div style={styles.field}>
          <label className="ef-label">Cantidad</label>
          <select
            value={quantity}
            onChange={(e) => setQuantity(Number(e.target.value))}
            className="ef-input"
          >
            {Array.from({ length: maxQty }, (_, i) => i + 1).map((n) => (
              <option key={n} value={n}>{n}</option>
            ))}
          </select>
        </div>

        <div style={styles.field}>
          <label className="ef-label">ID de método de pago (Stripe)</label>
          <input
            type="text"
            placeholder="pm_card_visa"
            value={paymentMethodId}
            onChange={(e) => setPaymentMethodId(e.target.value)}
            className="ef-input"
            required
          />
          <span style={styles.hint}>Para pruebas usar: <code style={styles.code}>pm_card_visa</code></span>
        </div>

        <div style={styles.totalRow}>
          <span style={styles.totalLabel}>Total a pagar</span>
          <strong style={styles.totalAmount}>{formatPrice(total)}</strong>
        </div>

        <button type="submit" disabled={isPending || submitted} className="ef-btn ef-btn-full">
          {isPending ? 'Procesando...' : submitted ? 'Solicitud enviada' : `Confirmar compra — ${formatPrice(total)}`}
        </button>
        {submitted && !isPending && (
          <p style={styles.submittedHint}>
            Si hubo un error, revisa{' '}
            <button style={styles.linkBtn} onClick={() => navigate('/orders')}>Mis órdenes</button>
            {' '}antes de intentar de nuevo.
          </p>
        )}
      </form>
    </div>
  )
}

const styles: Record<string, React.CSSProperties> = {
  container:   { maxWidth: '520px', margin: '0 auto' },
  feedback:    { textAlign: 'center', padding: '4rem', color: t.textMuted },
  back:        { background: 'none', border: 'none', cursor: 'pointer', color: t.accent, marginBottom: '1.25rem', padding: 0, fontSize: '0.9rem', fontWeight: 500 },
  heading:     { fontSize: '1.75rem', fontWeight: 700, color: t.text, marginBottom: '1.25rem' },
  summary:     { background: t.surface, border: `1px solid ${t.border}`, borderRadius: '10px', padding: '1.25rem', marginBottom: '1.5rem' },
  eventTitle:  { fontSize: '1.05rem', fontWeight: 600, color: t.text, margin: '0 0 0.25rem' },
  eventMeta:   { color: t.textMuted, fontSize: '0.875rem', margin: '0 0 0.875rem' },
  divider:     { height: '1px', background: t.border, margin: '0.875rem 0' },
  ticketName:  { fontWeight: 500, margin: 0, color: t.text },
  ticketPrice: { color: t.accent, fontWeight: 600, margin: '0.2rem 0 0', fontSize: '0.9rem' },
  form:        { display: 'flex', flexDirection: 'column', gap: '1.25rem' },
  field:       { display: 'flex', flexDirection: 'column', gap: '0.35rem' },
  hint:        { color: t.textDim, fontSize: '0.8rem', marginTop: '0.25rem' },
  code:        { background: t.surface2, padding: '0.1rem 0.4rem', borderRadius: '3px', fontFamily: 'monospace', fontSize: '0.8rem', color: t.text },
  totalRow:    { display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '0.875rem 0', borderTop: `1px solid ${t.border}` },
  totalLabel:  { color: t.textMuted, fontSize: '0.95rem' },
  totalAmount: { fontSize: '1.15rem', color: t.text },
  linkBtn:       { background: 'none', border: 'none', color: t.accent, cursor: 'pointer', textDecoration: 'underline', fontSize: 'inherit', padding: 0 },
  submittedHint: { fontSize: '0.82rem', color: t.textMuted, textAlign: 'center' as const, lineHeight: 1.5 },
}
