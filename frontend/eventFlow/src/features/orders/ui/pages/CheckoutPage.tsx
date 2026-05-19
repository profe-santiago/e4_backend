import { useState } from 'react'
import { useParams, useLocation, useNavigate } from 'react-router-dom'
import { loadStripe } from '@stripe/stripe-js'
import { Elements, CardNumberElement, CardExpiryElement, CardCvcElement, useStripe, useElements } from '@stripe/react-stripe-js'
import { useEventDetail } from '@/features/events/ui/hooks/useEventDetail'
import { useTicketTypesByEvent } from '@/features/events/ui/hooks/useTicketTypesByEvent'
import { useCreateOrder } from '../hooks/useCreateOrder'
import { useCreatePaymentIntent } from '@/features/payments/ui/hooks/useCreatePaymentIntent'
import { t } from '@/shared/config/theme'
import type { Event } from '@/features/events/domain/entities/Event'
import type { TicketType } from '@/features/events/domain/entities/TicketType'

const stripePromise = loadStripe(import.meta.env.VITE_STRIPE_PUBLISHABLE_KEY as string)

const STRIPE_ERROR_MESSAGES: Record<string, string> = {
  insufficient_funds:         'Fondos insuficientes. Verifica el saldo de tu tarjeta.',
  card_declined:              'Tarjeta declinada. Intenta con otra tarjeta.',
  expired_card:               'La tarjeta está vencida.',
  incorrect_cvc:              'El código de seguridad es incorrecto.',
  processing_error:           'Error al procesar el pago. Intenta de nuevo.',
  do_not_honor:               'Tarjeta declinada por el banco. Contacta a tu banco.',
  card_not_supported:         'Esta tarjeta no es compatible. Intenta con otra.',
  currency_not_supported:     'Esta tarjeta no acepta la moneda de la transacción.',
  duplicate_transaction:      'Transacción duplicada. Espera unos minutos antes de reintentar.',
  fraudulent:                 'Transacción bloqueada por seguridad. Contacta a tu banco.',
  generic_decline:            'Tarjeta declinada. Intenta con otra tarjeta.',
  incorrect_number:           'El número de tarjeta es incorrecto.',
  invalid_expiry_month:       'El mes de vencimiento es inválido.',
  invalid_expiry_year:        'El año de vencimiento es inválido.',
  invalid_cvc:                'El código de seguridad es inválido.',
  lost_card:                  'Tarjeta reportada como perdida. Usa otra tarjeta.',
  stolen_card:                'Tarjeta reportada como robada. Usa otra tarjeta.',
  withdrawal_count_limit_exceeded: 'Límite de transacciones excedido. Intenta mañana o con otra tarjeta.',
}

const stripeErrorMessage = (code?: string): string =>
  (code && STRIPE_ERROR_MESSAGES[code]) ?? 'El pago fue rechazado. Verifica los datos e intenta de nuevo.'

const IS_TEST_MODE = import.meta.env.VITE_STRIPE_PUBLISHABLE_KEY?.startsWith('pk_test_')

const TEST_CARDS = [
  { label: 'Pago exitoso (sin 3DS)',  number: '4242 4242 4242 4242' },
  { label: 'Pago exitoso (con 3DS)',  number: '4000 0025 0000 3155' },
  { label: 'Fondos insuficientes',    number: '4000 0000 0000 9995' },
  { label: 'Tarjeta declinada',       number: '4000 0000 0000 0002' },
  { label: 'Tarjeta vencida',         number: '4000 0000 0000 0069' },
]

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

interface PaymentFormProps {
  event: Event
  ticketType: TicketType
}

const PaymentForm = ({ event, ticketType }: PaymentFormProps) => {
  const navigate = useNavigate()
  const stripe = useStripe()
  const elements = useElements()

  const { mutate: createOrder, isPending: isCreatingOrder } = useCreateOrder()
  const { mutateAsync: createIntent, isPending: isCreatingIntent } = useCreatePaymentIntent()

  const [quantity, setQuantity] = useState(1)
  const [cardError, setCardError] = useState('')
  const [submitted, setSubmitted] = useState(false)

  const maxQty = Math.min(ticketType.availableQuantity, 10)
  const total = ticketType.price * quantity
  const isPending = isCreatingIntent || isCreatingOrder

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!stripe || !elements || submitted || isPending) return

    setCardError('')

    const cardNumber = elements.getElement(CardNumberElement)
    if (!cardNumber) return

    // Paso 1: crear el PaymentIntent en el backend
    let clientSecret: string
    let paymentIntentId: string
    try {
      const intent = await createIntent({ amount: total, currency: 'USD' })
      clientSecret = intent.clientSecret
      paymentIntentId = intent.paymentIntentId
    } catch {
      setCardError('No se pudo iniciar el pago. Intenta de nuevo.')
      return
    }

    // Paso 2: confirmar en el browser — Stripe maneja 3DS automáticamente
    const { error, paymentIntent } = await stripe.confirmCardPayment(clientSecret, {
      payment_method: { card: cardNumber },
    })

    if (error) {
      setCardError(stripeErrorMessage(error.code))
      return
    }

    if (paymentIntent?.status !== 'succeeded') {
      setCardError('El pago no pudo completarse. Intenta de nuevo.')
      return
    }

    // Paso 3: crear la orden con el paymentIntentId ya confirmado
    setSubmitted(true)
    createOrder({
      items: [{ eventId: event.id, ticketTypeId: ticketType.id, quantity }],
      paymentIntentId,
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

      {IS_TEST_MODE && (
        <div style={styles.testPanel}>
          <p style={styles.testTitle}>Tarjetas de prueba — fecha futura · CVC cualquiera</p>
          {TEST_CARDS.map((card) => (
            <div key={card.number} style={styles.testRow}>
              <code style={styles.testNumber}>{card.number}</code>
              <span style={styles.testLabel}>{card.label}</span>
            </div>
          ))}
        </div>
      )}

      <div style={styles.totalRow}>
        <span style={styles.totalLabel}>Total a pagar</span>
        <strong style={styles.totalAmount}>{formatPrice(total)}</strong>
      </div>

      <button
        type="submit"
        disabled={!stripe || isPending || submitted}
        className="ef-btn ef-btn-full"
      >
        {isCreatingIntent
          ? 'Iniciando pago...'
          : isCreatingOrder
            ? 'Confirmando orden...'
            : submitted
              ? 'Solicitud enviada'
              : `Confirmar compra — ${formatPrice(total)}`}
      </button>

      {submitted && !isPending && (
        <p style={styles.submittedHint}>
          Si hubo un error, revisa{' '}
          <button style={styles.linkBtn} onClick={() => navigate('/orders')}>Mis órdenes</button>
          {' '}antes de intentar de nuevo.
        </p>
      )}
    </form>
  )
}

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
  totalRow:     { display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '0.875rem 0', borderTop: `1px solid ${t.border}` },
  totalLabel:   { color: t.textMuted, fontSize: '0.95rem' },
  totalAmount:  { fontSize: '1.15rem', color: t.text },
  linkBtn:       { background: 'none', border: 'none', color: t.accent, cursor: 'pointer', textDecoration: 'underline', fontSize: 'inherit', padding: 0 },
  submittedHint: { fontSize: '0.82rem', color: t.textMuted, textAlign: 'center' as const, lineHeight: 1.5 },
  testPanel:     { background: t.surface2, border: `1px solid ${t.border}`, borderRadius: '8px', padding: '0.875rem', display: 'flex', flexDirection: 'column' as const, gap: '0.4rem' },
  testTitle:     { fontSize: '0.75rem', color: t.textDim, margin: '0 0 0.25rem', fontWeight: 600, textTransform: 'uppercase' as const, letterSpacing: '0.05em' },
  testRow:       { display: 'flex', justifyContent: 'space-between', alignItems: 'center', gap: '0.5rem' },
  testNumber:    { fontFamily: 'monospace', fontSize: '0.8rem', color: t.text, letterSpacing: '0.05em' },
  testLabel:     { fontSize: '0.75rem', color: t.textMuted, textAlign: 'right' as const },
}
