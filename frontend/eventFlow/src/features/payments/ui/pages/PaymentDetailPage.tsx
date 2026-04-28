import { useParams, useNavigate, Link } from 'react-router-dom'
import { usePayment } from '../hooks/usePayment'
import type { PaymentStatus } from '../../domain/entities/Payment'

const formatAmount = (amount: number, currency: string) =>
  new Intl.NumberFormat('es-AR', { style: 'currency', currency }).format(amount)

const formatDate = (iso: string) =>
  new Date(iso).toLocaleString('es-AR', { day: '2-digit', month: 'short', year: 'numeric', hour: '2-digit', minute: '2-digit' })

const statusConfig: Record<PaymentStatus, { label: string; color: string; background: string }> = {
  PENDING: { label: 'Pendiente', color: '#92400e', background: '#fef3c7' },
  APPROVED: { label: 'Aprobado', color: '#065f46', background: '#d1fae5' },
  REJECTED: { label: 'Rechazado', color: '#991b1b', background: '#fee2e2' },
  REFUNDED: { label: 'Reembolsado', color: '#1e40af', background: '#dbeafe' },
}

export const PaymentDetailPage = () => {
  const { id } = useParams<{ id: string }>()
  const navigate = useNavigate()
  const { data: payment, isLoading, isError } = usePayment(id ?? '')

  if (isLoading) return <div style={styles.feedback}>Cargando pago...</div>
  if (isError || !payment) return <div style={styles.error}>No se pudo cargar el pago.</div>

  const config = statusConfig[payment.status]

  return (
    <div style={styles.container}>
      <button style={styles.back} onClick={() => navigate(-1)}>← Volver</button>

      <h1 style={styles.heading}>
        Pago <span style={styles.id}>#{payment.id.slice(0, 8).toUpperCase()}</span>
      </h1>
      <p style={styles.date}>Creado el {formatDate(payment.createdAt)}</p>

      <span style={{ ...styles.badge, color: config.color, background: config.background }}>
        {config.label}
      </span>

      <section style={styles.section}>
        <div style={styles.row}>
          <span style={styles.label}>Monto</span>
          <span style={styles.value}>{formatAmount(payment.amount, payment.currency)}</span>
        </div>
        <div style={styles.row}>
          <span style={styles.label}>Moneda</span>
          <span style={styles.value}>{payment.currency}</span>
        </div>
        {payment.paymentMethodId && (
          <div style={styles.row}>
            <span style={styles.label}>Método de pago</span>
            <span style={{ ...styles.value, fontFamily: 'monospace' }}>{payment.paymentMethodId}</span>
          </div>
        )}
        {payment.transactionId && (
          <div style={styles.row}>
            <span style={styles.label}>ID transacción</span>
            <span style={{ ...styles.value, fontFamily: 'monospace' }}>{payment.transactionId}</span>
          </div>
        )}
        {payment.updatedAt && (
          <div style={styles.row}>
            <span style={styles.label}>Última actualización</span>
            <span style={styles.value}>{formatDate(payment.updatedAt)}</span>
          </div>
        )}
      </section>

      {payment.orderId && (
        <Link to={`/orders/${payment.orderId}`} style={styles.orderLink}>
          Ver orden →
        </Link>
      )}
    </div>
  )
}

const styles: Record<string, React.CSSProperties> = {
  container: { maxWidth: '600px', margin: '0 auto', padding: '2rem 1rem' },
  feedback: { textAlign: 'center', padding: '4rem', color: '#555' },
  error: { textAlign: 'center', padding: '4rem', color: '#e53e3e' },
  back: { background: 'none', border: 'none', cursor: 'pointer', color: '#3182ce', marginBottom: '1rem', padding: 0, fontSize: '0.9rem' },
  heading: { fontSize: '1.5rem', fontWeight: 700, margin: '0 0 0.25rem' },
  id: { fontFamily: 'monospace', color: '#555' },
  date: { color: '#888', fontSize: '0.875rem', margin: '0 0 1rem' },
  badge: { display: 'inline-block', padding: '0.25rem 0.75rem', borderRadius: '9999px', fontSize: '0.8rem', fontWeight: 600, marginBottom: '1.5rem' },
  section: { border: '1px solid #e2e8f0', borderRadius: '8px', padding: '1.25rem', marginBottom: '1rem' },
  row: { display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '0.5rem 0', borderBottom: '1px solid #f0f0f0' },
  label: { color: '#666', fontSize: '0.875rem' },
  value: { fontWeight: 500, fontSize: '0.9rem' },
  orderLink: { display: 'inline-block', color: '#3182ce', textDecoration: 'none', fontSize: '0.9rem' },
}
