import { useParams, useNavigate, Link } from 'react-router-dom'
import { useOrderDetail } from '../hooks/useOrderDetail'
import { useOrderActions } from '../hooks/useOrderActions'
import { OrderStatusTracker } from '../components/OrderStatusTracker'
import { usePaymentByOrder } from '@/features/payments/ui/hooks/usePaymentByOrder'
import type { PaymentStatus } from '@/features/payments/domain/entities/Payment'

const formatPrice = (amount: number) =>
  new Intl.NumberFormat('es-AR', { style: 'currency', currency: 'ARS' }).format(amount)

const formatDate = (iso: string) =>
  new Date(iso).toLocaleString('es-AR', { day: '2-digit', month: 'short', year: 'numeric', hour: '2-digit', minute: '2-digit' })

const paymentStatusConfig: Record<PaymentStatus, { label: string; color: string; background: string }> = {
  PENDING: { label: 'Pendiente', color: '#92400e', background: '#fef3c7' },
  APPROVED: { label: 'Aprobado', color: '#065f46', background: '#d1fae5' },
  REJECTED: { label: 'Rechazado', color: '#991b1b', background: '#fee2e2' },
  REFUNDED: { label: 'Reembolsado', color: '#1e40af', background: '#dbeafe' },
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
      <p style={styles.date}>Creada el {formatDate(order.createdAt)}</p>

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
          <span>Total</span>
          <span style={styles.totalAmount}>{formatPrice(order.totalAmount)}</span>
        </div>
      </section>

      {order.status === 'CONFIRMED' && (
        <Link to="/tickets" style={styles.ticketsLink}>Ver mis tickets →</Link>
      )}

      {order.status === 'CONFIRMED' && (
        <section style={styles.section}>
          <h2 style={styles.sectionTitle}>Pago</h2>
          {isPaymentLoading && (
            <p style={styles.paymentMeta}>Cargando información de pago...</p>
          )}
          {isPaymentError && (
            <p style={styles.paymentMeta}>No se pudo cargar el pago.</p>
          )}
          {payment && (() => {
            const cfg = paymentStatusConfig[payment.status]
            return (
              <div style={styles.paymentContent}>
                <div style={styles.paymentRow}>
                  <span style={styles.paymentLabel}>Estado</span>
                  <span style={{ ...styles.paymentBadge, color: cfg.color, background: cfg.background }}>
                    {cfg.label}
                  </span>
                </div>
                <div style={styles.paymentRow}>
                  <span style={styles.paymentLabel}>Monto</span>
                  <span style={styles.paymentValue}>{formatPrice(payment.amount)}</span>
                </div>
                <div style={styles.paymentLinkRow}>
                  <Link to={`/payments/${payment.id}`} style={styles.paymentLink}>
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
          <button
            style={styles.cancelBtn}
            disabled={cancel.isPending}
            onClick={() => cancel.mutate()}
          >
            {cancel.isPending ? 'Cancelando...' : 'Cancelar orden'}
          </button>
        )}
        {order.status === 'CONFIRMED' && (
          <button
            style={styles.refundBtn}
            disabled={refund.isPending}
            onClick={() => refund.mutate()}
          >
            {refund.isPending ? 'Procesando...' : 'Solicitar reembolso'}
          </button>
        )}
      </div>
    </div>
  )
}

const styles: Record<string, React.CSSProperties> = {
  container: { maxWidth: '700px', margin: '0 auto', padding: '2rem 1rem' },
  feedback: { textAlign: 'center', padding: '4rem', color: '#555' },
  error: { textAlign: 'center', padding: '4rem', color: '#e53e3e' },
  back: { background: 'none', border: 'none', cursor: 'pointer', color: '#3182ce', marginBottom: '1rem', padding: 0, fontSize: '0.9rem' },
  heading: { fontSize: '1.5rem', fontWeight: 700, margin: '0 0 0.25rem' },
  id: { fontFamily: 'monospace', color: '#555' },
  date: { color: '#888', fontSize: '0.875rem', margin: '0 0 1.5rem' },
  section: { border: '1px solid #e2e8f0', borderRadius: '8px', padding: '1.25rem', marginBottom: '1rem' },
  sectionTitle: { fontSize: '1rem', fontWeight: 600, marginBottom: '0.75rem' },
  item: { display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '0.5rem 0', borderBottom: '1px solid #f0f0f0' },
  itemLabel: { fontWeight: 500, margin: 0 },
  itemMeta: { color: '#888', fontSize: '0.85rem', margin: '0.2rem 0 0' },
  itemPrice: { fontWeight: 600, margin: 0 },
  total: { display: 'flex', justifyContent: 'space-between', paddingTop: '0.75rem', fontWeight: 600 },
  totalAmount: { fontSize: '1.1rem' },
  ticketsLink: { display: 'inline-block', color: '#3182ce', marginBottom: '1rem', textDecoration: 'none' },
  actions: { display: 'flex', gap: '0.75rem', marginTop: '1rem' },
  cancelBtn: { padding: '0.6rem 1.25rem', border: '1px solid #e53e3e', borderRadius: '4px', color: '#e53e3e', background: 'none', cursor: 'pointer' },
  refundBtn: { padding: '0.6rem 1.25rem', border: '1px solid #3182ce', borderRadius: '4px', color: '#3182ce', background: 'none', cursor: 'pointer' },
  paymentMeta: { color: '#888', fontSize: '0.875rem', margin: 0 },
  paymentContent: { display: 'flex', flexDirection: 'column', gap: '0.5rem' },
  paymentRow: { display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '0.4rem 0', borderBottom: '1px solid #f0f0f0' },
  paymentLabel: { color: '#666', fontSize: '0.875rem' },
  paymentValue: { fontWeight: 500, fontSize: '0.9rem' },
  paymentBadge: { display: 'inline-block', padding: '0.2rem 0.6rem', borderRadius: '9999px', fontSize: '0.75rem', fontWeight: 600 },
  paymentLinkRow: { paddingTop: '0.5rem' },
  paymentLink: { color: '#3182ce', textDecoration: 'none', fontSize: '0.875rem' },
}
