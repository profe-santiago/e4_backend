import { useState } from 'react'
import { useParams, useLocation, useNavigate } from 'react-router-dom'
import { loadStripe } from '@stripe/stripe-js'
import { Elements, CardNumberElement, CardExpiryElement, CardCvcElement, useStripe, useElements } from '@stripe/react-stripe-js'
import { useEventDetail } from '@/features/events/ui/hooks/useEventDetail'
import { useTicketTypesByEvent } from '@/features/events/ui/hooks/useTicketTypesByEvent'
import { useCreateOrder } from '../hooks/useCreateOrder'
import { t } from '@/shared/config/theme'
import type { Event } from '@/features/events/domain/entities/Event'
import type { TicketType } from '@/features/events/domain/entities/TicketType'

// loadStripe se llama UNA sola vez fuera del componente.
// Retorna una promesa que <Elements> resuelve internamente.
// Esto garantiza que el script de Stripe.js se cargue una sola vez
// desde los servidores de Stripe (requisito de PCI compliance).
const stripePromise = loadStripe(import.meta.env.VITE_STRIPE_PUBLISHABLE_KEY as string)

// Estilo compartido para los tres iframes de Stripe.
// Cada campo (número, expiración, CVC) vive en su propio iframe,
// por eso los colores se pasan como config en lugar de CSS externo.
const fieldStyle = {
  style: {
    base: {
      color: t.text,
      fontFamily: 'inherit',
      fontSize: '15px',
      fontSmoothing: 'antialiased',
      '::placeholder': { color: t.textDim },
    },
    invalid: { color: t.error, iconColor: t.error },
  },
}

interface LocationState {
  ticketTypeId: number
}

const formatPrice = (price: number) =>
  new Intl.NumberFormat('en-US', { style: 'currency', currency: 'USD' }).format(price)

// ── Formulario interno ────────────────────────────────────────────────────────
// Debe vivir DENTRO de <Elements> para poder usar useStripe() y useElements().
// Recibe los datos ya cargados como props para no repetir fetching.
interface PaymentFormProps {
  event: Event
  ticketType: TicketType
}

const PaymentForm = ({ event, ticketType }: PaymentFormProps) => {
  const navigate = useNavigate()
  const stripe = useStripe()     // contexto de Stripe.js (disponible porque estamos dentro de <Elements>)
  const elements = useElements() // acceso a los elementos montados (CardElement)

  const { mutate: createOrder, isPending } = useCreateOrder()

  const [quantity, setQuantity] = useState(1)
  const [cardError, setCardError] = useState('')
  const [submitted, setSubmitted] = useState(false)

  const maxQty = Math.min(ticketType.availableQuantity, 10)
  const total = ticketType.price * quantity

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!stripe || !elements || submitted || isPending) return

    setCardError('')

    const cardNumber = elements.getElement(CardNumberElement)
    if (!cardNumber) return

    const { paymentMethod, error } = await stripe.createPaymentMethod({
      type: 'card',
      card: cardNumber,
    })

    if (error) {
      setCardError(error.message ?? 'Error al procesar la tarjeta')
      return
    }

    setSubmitted(true)
    createOrder({
      items: [{ eventId: event.id, ticketTypeId: ticketType.id, quantity }],
      paymentMethodId: paymentMethod.id,
    })
  }

  return (
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
        <label className="ef-label">Número de tarjeta</label>
        <div style={styles.cardWrapper}>
          <CardNumberElement options={fieldStyle} />
        </div>
      </div>

      <div style={styles.cardRow}>
        <div style={{ ...styles.field, flex: 2 }}>
          <label className="ef-label">Vencimiento</label>
          <div style={styles.cardWrapper}>
            <CardExpiryElement options={fieldStyle} />
          </div>
        </div>
        <div style={{ ...styles.field, flex: 1 }}>
          <label className="ef-label">CVC</label>
          <div style={styles.cardWrapper}>
            <CardCvcElement options={fieldStyle} />
          </div>
        </div>
      </div>

      {cardError && <span style={styles.errorMsg}>{cardError}</span>}
      <span style={styles.hint}>Para pruebas: <code style={styles.code}>4242 4242 4242 4242</code> · cualquier fecha futura · cualquier CVC</span>

      <div style={styles.totalRow}>
        <span style={styles.totalLabel}>Total a pagar</span>
        <strong style={styles.totalAmount}>{formatPrice(total)}</strong>
      </div>

      <button
        type="submit"
        disabled={!stripe || isPending || submitted}
        className="ef-btn ef-btn-full"
      >
        {isPending ? 'Procesando...' : submitted ? 'Solicitud enviada' : `Confirmar compra — ${formatPrice(total)}`}
      </button>

      {submitted && !isPending && (
        <p style={styles.submittedHint}>
          Si hubo un error, revisá{' '}
          <button style={styles.linkBtn} onClick={() => navigate('/orders')}>Mis órdenes</button>
          {' '}antes de intentar de nuevo.
        </p>
      )}
    </form>
  )
}

// ── Página principal ──────────────────────────────────────────────────────────
// Carga los datos del evento y tipo de ticket, luego monta el provider
// <Elements> que le da contexto de Stripe a PaymentForm.
export const CheckoutPage = () => {
  const { id: eventId } = useParams<{ id: string }>()
  const location = useLocation()
  const navigate = useNavigate()
  const state = location.state as LocationState | null

  const { data: event, isLoading: isLoadingEvent } = useEventDetail(eventId ?? '')
  const { data: ticketTypes = [], isLoading: isLoadingTickets } = useTicketTypesByEvent(eventId ?? '')

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

      {/* <Elements> provee el contexto de Stripe a todos los componentes hijos.
          stripePromise se resuelve una vez y Elements lo mantiene. */}
      <Elements stripe={stripePromise}>
        <PaymentForm event={event} ticketType={ticketType} />
      </Elements>
    </div>
  )
}

const styles: Record<string, React.CSSProperties> = {
  container:    { maxWidth: '520px', margin: '0 auto' },
  feedback:     { textAlign: 'center', padding: '4rem', color: t.textMuted },
  back:         { background: 'none', border: 'none', cursor: 'pointer', color: t.accent, marginBottom: '1.25rem', padding: 0, fontSize: '0.9rem', fontWeight: 500 },
  heading:      { fontSize: '1.75rem', fontWeight: 700, color: t.text, marginBottom: '1.25rem' },
  summary:      { background: t.surface, border: `1px solid ${t.border}`, borderRadius: '10px', padding: '1.25rem', marginBottom: '1.5rem' },
  eventTitle:   { fontSize: '1.05rem', fontWeight: 600, color: t.text, margin: '0 0 0.25rem' },
  eventMeta:    { color: t.textMuted, fontSize: '0.875rem', margin: '0 0 0.875rem' },
  divider:      { height: '1px', background: t.border, margin: '0.875rem 0' },
  ticketName:   { fontWeight: 500, margin: 0, color: t.text },
  ticketPrice:  { color: t.accent, fontWeight: 600, margin: '0.2rem 0 0', fontSize: '0.9rem' },
  form:         { display: 'flex', flexDirection: 'column', gap: '1.25rem' },
  field:        { display: 'flex', flexDirection: 'column', gap: '0.35rem' },
  cardRow:      { display: 'flex', gap: '0.75rem' },
  cardWrapper:  {
    padding: '0.75rem 0.875rem',
    background: t.surface,
    border: `1px solid ${t.border}`,
    borderRadius: '8px',
  },
  errorMsg:     { color: t.error, fontSize: '0.8rem' },
  hint:         { color: t.textDim, fontSize: '0.8rem', marginTop: '0.25rem' },
  code:         { background: t.surface2, padding: '0.1rem 0.4rem', borderRadius: '3px', fontFamily: 'monospace', fontSize: '0.8rem', color: t.text },
  totalRow:     { display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '0.875rem 0', borderTop: `1px solid ${t.border}` },
  totalLabel:   { color: t.textMuted, fontSize: '0.95rem' },
  totalAmount:  { fontSize: '1.15rem', color: t.text },
  linkBtn:      { background: 'none', border: 'none', color: t.accent, cursor: 'pointer', textDecoration: 'underline', fontSize: 'inherit', padding: 0 },
  submittedHint:{ fontSize: '0.82rem', color: t.textMuted, textAlign: 'center' as const, lineHeight: 1.5 },
}
