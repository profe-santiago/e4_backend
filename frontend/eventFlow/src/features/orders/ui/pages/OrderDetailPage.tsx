import { useMemo } from 'react'
import { useParams, useNavigate, Link } from 'react-router-dom'
import { useQueries } from '@tanstack/react-query'
import { useOrderDetail } from '../hooks/useOrderDetail'
import { useOrderActions } from '../hooks/useOrderActions'
import { OrderStatusTracker } from '../components/OrderStatusTracker'
import { usePaymentByOrder } from '@/features/payments/ui/hooks/usePaymentByOrder'
import { useTicketsByOrder } from '@/features/tickets/ui/hooks/useTicketsByOrder'
import { useTicketTypeRepository } from '@/core/di/TicketTypeContext'
import { ListTicketTypesByEventUseCase } from '@/features/events/application/use-cases/ListTicketTypesByEventUseCase'
import type { PaymentStatus } from '@/features/payments/domain/entities/Payment'
import type { TicketType } from '@/features/events/domain/entities/TicketType'
import { t } from '@/shared/config/theme'
import { formatDateTime } from '@/shared/utils/formatDate'

const formatPrice = (amount: number) =>
  new Intl.NumberFormat('en-US', { style: 'currency', currency: 'USD' }).format(amount)

const formatDate = formatDateTime

const PAYMENT_STATUS: Record<PaymentStatus, { label: string; color: string }> = {
  PENDING:  { label: 'Pendiente',   color: '#d69e2e' },
  APPROVED: { label: 'Aprobado',    color: '#38a169' },
  REJECTED: { label: 'Rechazado',   color: '#e53e3e' },
  REFUNDED: { label: 'Reembolsado', color: t.accent  },
}

export const OrderDetailPage = () => {
  const { id } = useParams<{ id: string }>()
  const navigate = useNavigate()
  const { data: order, isLoading, isError } = useOrderDetail(id ?? '')
  const { cancel, refund } = useOrderActions(id ?? '')
  const { data: payment, isLoading: isPaymentLoading, isError: isPaymentError, isFetching: isPaymentFetching } = usePaymentByOrder(
    order?.id ?? '',
    order?.status === 'CONFIRMED',
  )
  const { data: orderTickets = [] } = useTicketsByOrder(order?.id ?? '', order?.status === 'CONFIRMED')
  const hasUsedOrExpiredTickets = orderTickets.some(
    t => t.status === 'USED' || t.status === 'EXPIRED'
  )

  const ticketTypeRepo = useTicketTypeRepository()
  const uniqueEventIds = useMemo(
    () => [...new Set((order?.items ?? []).map(i => i.eventId))],
    [order?.items],
  )
  const ticketTypeQueries = useQueries({
    queries: uniqueEventIds.map(eventId => ({
      queryKey: ['ticket-types', eventId],
      queryFn: () => new ListTicketTypesByEventUseCase(ticketTypeRepo).execute(eventId),
      enabled: !!eventId,
    })),
  })
  const typeNameMap = useMemo(() => {
    const map = new Map<number, string>()
    ticketTypeQueries.forEach(q => {
      q.data?.forEach((tt: TicketType) => map.set(tt.id, tt.name))
    })
    return map
  }, [ticketTypeQueries])

  if (isLoading) return <div style={styles.feedback}>Cargando orden...</div>
  if (isError || !order) return <div style={styles.error}>No se pudo cargar la orden.</div>

  return (
    <div style={styles.container}>
      <button style={styles.back} onClick={() => navigate(-1)}>← Volver</button>

      <h1 style={styles.heading}>
        Orden <span style={styles.id}>#{order.id.slice(0, 8).toUpperCase()}</span>
      </h1>
      <p style={styles.date}>{formatDate(order.createdAt)}</p>

      <OrderStatusTracker status={order.status} />

      <section style={styles.section}>
        <h2 style={styles.sectionTitle}>Entradas</h2>
        {order.items.map((item, i) => (
          <div key={i} style={styles.item}>
            <div>
              <p style={styles.itemLabel}>
                {typeNameMap.get(item.ticketTypeId) ?? `Ticket #${item.ticketTypeId}`}
              </p>
              <p style={styles.itemMeta}>Cantidad: {item.quantity}</p>
            </div>
            <p style={styles.itemPrice}>{formatPrice(item.unitPrice * item.quantity)}</p>
          </div>
        ))}
        <div style={styles.total}>
          <span style={{ color: t.textMuted }}>Total</span>
          <span style={styles.totalAmount}>{formatPrice(order.totalAmount)}</span>
        </div>
      </section>

      {order.status === 'CONFIRMED' && (
        <Link to="/tickets" className="ef-link" style={{ display: 'inline-block', marginBottom: '1rem', fontSize: '0.9rem' }}>
          Ver mis tickets →
        </Link>
      )}

      {order.status === 'CONFIRMED' && (
        <section style={styles.section}>
          <h2 style={styles.sectionTitle}>Pago</h2>
          {(isPaymentLoading || (!payment && isPaymentFetching)) && (
            <p style={styles.paymentMeta}>Procesando pago... esto puede tardar unos segundos.</p>
          )}
          {!payment && isPaymentError && !isPaymentFetching && (
            <p style={styles.paymentMeta}>
              El pago está tardando más de lo esperado.{' '}
              <button
                onClick={() => window.location.reload()}
                style={{ background: 'none', border: 'none', color: 'inherit', cursor: 'pointer', textDecoration: 'underline', padding: 0, fontSize: 'inherit' }}
              >
                Recarga la página
              </button>{' '}
              en unos minutos para ver el estado.
            </p>
          )}
          {payment && (() => {
            const cfg = PAYMENT_STATUS[payment.status]
            return (
              <div style={styles.paymentContent}>
                <div style={styles.paymentRow}>
                  <span style={styles.paymentLabel}>Estado</span>
                  <span style={{ ...styles.badge, background: cfg.color }}>
                    {cfg.label}
                    {payment.status === 'PENDING' && ' · procesando...'}
                  </span>
                </div>
                <div style={styles.paymentRow}>
                  <span style={styles.paymentLabel}>Monto</span>
                  <span style={styles.paymentValue}>{formatPrice(payment.amount)}</span>
                </div>
                <div style={{ paddingTop: '0.5rem' }}>
                  <Link to={`/payments/${payment.id}`} className="ef-link" style={{ fontSize: '0.875rem' }}>
                    Ver detalle del pago →
                  </Link>
                </div>
              </div>
            )
          })()}
        </section>
      )}

      <div style={styles.actions}>
        {order.status === 'PENDING' && (
          <button className="ef-btn-ghost" disabled={cancel.isPending} onClick={() => cancel.mutate()}>
            {cancel.isPending ? 'Cancelando...' : 'Cancelar orden'}
          </button>
        )}
        {order.status === 'CONFIRMED' && !hasUsedOrExpiredTickets && (
          <button className="ef-btn-ghost" disabled={refund.isPending} onClick={() => refund.mutate()}>
            {refund.isPending ? 'Procesando...' : 'Solicitar reembolso'}
          </button>
        )}
        {order.status === 'CONFIRMED' && hasUsedOrExpiredTickets && (
          <p style={styles.refundBlocked}>
            No es posible solicitar reembolso porque uno o más boletos ya fueron utilizados.
          </p>
        )}
        {order.status === 'REFUND_PENDING' && (
          <p style={styles.refundSent}>
            Reembolso en proceso — recibirás un email cuando Stripe confirme el pago.
          </p>
        )}
      </div>
    </div>
  )
}

const styles: Record<string, React.CSSProperties> = {
  container:     { maxWidth: '700px', margin: '0 auto' },
  feedback:      { textAlign: 'center', padding: '4rem', color: t.textMuted },
  error:         { textAlign: 'center', padding: '4rem', color: t.error },
  back:          { background: 'none', border: 'none', cursor: 'pointer', color: t.accent, marginBottom: '1rem', padding: 0, fontSize: '0.9rem', fontWeight: 500 },
  heading:       { fontSize: '1.5rem', fontWeight: 700, color: t.text, margin: '0 0 0.25rem' },
  id:            { fontFamily: 'monospace', color: t.textMuted },
  date:          { color: t.textMuted, fontSize: '0.875rem', margin: '0 0 1.5rem' },
  section:       { background: t.surface, border: `1px solid ${t.border}`, borderRadius: '8px', padding: '1.25rem', marginBottom: '1rem' },
  sectionTitle:  { fontSize: '0.95rem', fontWeight: 600, color: t.text, marginBottom: '0.75rem' },
  item:          { display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '0.5rem 0', borderBottom: `1px solid ${t.border}` },
  itemLabel:     { fontWeight: 500, margin: 0, color: t.text },
  itemMeta:      { color: t.textMuted, fontSize: '0.85rem', margin: '0.2rem 0 0' },
  itemPrice:     { fontWeight: 600, margin: 0, color: t.text },
  total:         { display: 'flex', justifyContent: 'space-between', paddingTop: '0.75rem', fontWeight: 600 },
  totalAmount:   { fontSize: '1.1rem', color: t.text },
  actions:       { display: 'flex', gap: '0.75rem', marginTop: '1rem', alignItems: 'center' },
  paymentMeta:   { color: t.textMuted, fontSize: '0.875rem', margin: 0 },
  paymentContent:{ display: 'flex', flexDirection: 'column', gap: '0.5rem' },
  paymentRow:    { display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '0.4rem 0', borderBottom: `1px solid ${t.border}` },
  paymentLabel:  { color: t.textMuted, fontSize: '0.875rem' },
  paymentValue:  { fontWeight: 500, fontSize: '0.9rem', color: t.text },
  badge:         { display: 'inline-block', padding: '0.2rem 0.6rem', borderRadius: '9999px', fontSize: '0.75rem', fontWeight: 600, color: '#fff' },
  refundSent:    { fontSize: '0.875rem', color: t.textMuted, margin: 0, lineHeight: 1.5 },
  refundBlocked: { fontSize: '0.875rem', color: t.textMuted, margin: 0, lineHeight: 1.5 },
}
