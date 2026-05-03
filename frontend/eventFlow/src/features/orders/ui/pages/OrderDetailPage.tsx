import { useParams, useNavigate, Link } from 'react-router-dom'
import { useOrderDetail } from '../hooks/useOrderDetail'
import { useOrderActions } from '../hooks/useOrderActions'
import { OrderStatusTracker } from '../components/OrderStatusTracker'
import { usePaymentByOrder } from '@/features/payments/ui/hooks/usePaymentByOrder'
import type { PaymentStatus } from '@/features/payments/domain/entities/Payment'
import { t } from '@/shared/config/theme'

const formatPrice = (amount: number) =>
  new Intl.NumberFormat('es-AR', { style: 'currency', currency: 'ARS' }).format(amount)

const formatDate = (iso: string) =>
  new Date(iso).toLocaleString('es-AR', { day: '2-digit', month: 'short', year: 'numeric', hour: '2-digit', minute: '2-digit' })

const PAYMENT_STATUS: Record<PaymentStatus, { label: string; color: string }> = {
  PENDING:  { label: 'Pendiente',    color: '#d69e2e' },
  APPROVED: { label: 'Aprobado',     color: '#38a169' },
  REJECTED: { label: 'Rechazado',    color: '#e53e3e' },
  REFUNDED: { label: 'Reembolsado',  color: t.accent  },
}

export const OrderDetailPage = () => {
  const { id } = useParams<{ id: string }>()
  const navigate = useNavigate()
  const { data: order, isLoading, isError } = useOrderDetail(id ?? '')
  const { cancel, refund } = useOrderActions(id ?? '')
  const { data: payment, isLoading: isPaymentLoading, isError: isPaymentError } = usePaymentByOrder(
    order?.id ?? '',
    order?.status === 'CONFIRMED',
  )

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
        <h2 style={styles.sectionTitle}>Ítems</h2>
        {order.items.map((item, i) => (
          <div key={i} style={styles.item}>
            <div>
              <p style={styles.itemLabel}>Ticket type #{item.ticketTypeId}</p>
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
          {isPaymentLoading && <p style={styles.paymentMeta}>Cargando información de pago...</p>}
          {isPaymentError && <p style={styles.paymentMeta}>No se pudo cargar el pago.</p>}
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
        {order.status === 'CONFIRMED' && (
          <button className="ef-btn-ghost" disabled={refund.isPending} onClick={() => refund.mutate()}>
            {refund.isPending ? 'Procesando...' : 'Solicitar reembolso'}
          </button>
        )}
      </div>
    </div>
  )
}

const styles: Record<string, React.CSSProperties> = {
  container:    { maxWidth: '700px', margin: '0 auto' },
  feedback:     { textAlign: 'center', padding: '4rem', color: t.textMuted },
  error:        { textAlign: 'center', padding: '4rem', color: t.error },
  back:         { background: 'none', border: 'none', cursor: 'pointer', color: t.accent, marginBottom: '1rem', padding: 0, fontSize: '0.9rem', fontWeight: 500 },
  heading:      { fontSize: '1.5rem', fontWeight: 700, color: t.text, margin: '0 0 0.25rem' },
  id:           { fontFamily: 'monospace', color: t.textMuted },
  date:         { color: t.textMuted, fontSize: '0.875rem', margin: '0 0 1.5rem' },
  section:      { background: t.surface, border: `1px solid ${t.border}`, borderRadius: '8px', padding: '1.25rem', marginBottom: '1rem' },
  sectionTitle: { fontSize: '0.95rem', fontWeight: 600, color: t.text, marginBottom: '0.75rem' },
  item:         { display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '0.5rem 0', borderBottom: `1px solid ${t.border}` },
  itemLabel:    { fontWeight: 500, margin: 0, color: t.text },
  itemMeta:     { color: t.textMuted, fontSize: '0.85rem', margin: '0.2rem 0 0' },
  itemPrice:    { fontWeight: 600, margin: 0, color: t.text },
  total:        { display: 'flex', justifyContent: 'space-between', paddingTop: '0.75rem', fontWeight: 600 },
  totalAmount:  { fontSize: '1.1rem', color: t.text },
  actions:      { display: 'flex', gap: '0.75rem', marginTop: '1rem' },
  paymentMeta:  { color: t.textMuted, fontSize: '0.875rem', margin: 0 },
  paymentContent: { display: 'flex', flexDirection: 'column', gap: '0.5rem' },
  paymentRow:   { display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '0.4rem 0', borderBottom: `1px solid ${t.border}` },
  paymentLabel: { color: t.textMuted, fontSize: '0.875rem' },
  paymentValue: { fontWeight: 500, fontSize: '0.9rem', color: t.text },
  badge:        { display: 'inline-block', padding: '0.2rem 0.6rem', borderRadius: '9999px', fontSize: '0.75rem', fontWeight: 600, color: '#fff' },
}
