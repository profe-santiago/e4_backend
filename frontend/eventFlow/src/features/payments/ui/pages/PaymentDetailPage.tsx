import { useParams, useNavigate, Link } from 'react-router-dom'
import { usePayment } from '../hooks/usePayment'
import type { PaymentStatus } from '../../domain/entities/Payment'
import { t } from '@/shared/config/theme'

const formatAmount = (amount: number, currency: string) =>
  new Intl.NumberFormat('es-AR', { style: 'currency', currency }).format(amount)

const formatDate = (iso: string) =>
  new Date(iso).toLocaleString('es-AR', { day: '2-digit', month: 'short', year: 'numeric', hour: '2-digit', minute: '2-digit' })

const STATUS_CONFIG: Record<PaymentStatus, { label: string; color: string }> = {
  PENDING:  { label: 'Pendiente',   color: '#d69e2e' },
  APPROVED: { label: 'Aprobado',    color: '#38a169' },
  REJECTED: { label: 'Rechazado',   color: '#e53e3e' },
  REFUNDED: { label: 'Reembolsado', color: t.accent  },
}

export const PaymentDetailPage = () => {
  const { id } = useParams<{ id: string }>()
  const navigate = useNavigate()
  const { data: payment, isLoading, isError } = usePayment(id ?? '')

  if (isLoading) return <div style={styles.feedback}>Cargando pago...</div>
  if (isError || !payment) return <div style={styles.error}>No se pudo cargar el pago.</div>

  const config = STATUS_CONFIG[payment.status]

  return (
    <div style={styles.container}>
      <button style={styles.back} onClick={() => navigate(-1)}>← Volver</button>

      <h1 style={styles.heading}>
        Pago <span style={styles.id}>#{payment.id.slice(0, 8).toUpperCase()}</span>
      </h1>
      <p style={styles.date}>{formatDate(payment.createdAt)}</p>

      <span style={{ ...styles.badge, background: config.color }}>{config.label}</span>

      <section style={styles.section}>
        <div style={styles.row}>
          <span style={styles.label}>Monto</span>
          <span style={styles.value}>{formatAmount(payment.amount, payment.currency)}</span>
        </div>
        <div style={styles.row}>
          <span style={styles.label}>Moneda</span>
          <span style={styles.value}>{payment.currency}</span>
        </div>
        {payment.paymentIntentId && (
          <div style={styles.row}>
            <span style={styles.label}>ID del pago</span>
            <span style={{ ...styles.value, fontFamily: 'monospace' }}>{payment.paymentIntentId}</span>
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
        <Link to={`/orders/${payment.orderId}`} className="ef-link" style={{ fontSize: '0.9rem' }}>
          Ver orden →
        </Link>
      )}
    </div>
  )
}

const styles: Record<string, React.CSSProperties> = {
  container: { maxWidth: '600px', margin: '0 auto' },
  feedback:  { textAlign: 'center', padding: '4rem', color: t.textMuted },
  error:     { textAlign: 'center', padding: '4rem', color: t.error },
  back:      { background: 'none', border: 'none', cursor: 'pointer', color: t.accent, marginBottom: '1rem', padding: 0, fontSize: '0.9rem', fontWeight: 500 },
  heading:   { fontSize: '1.5rem', fontWeight: 700, color: t.text, margin: '0 0 0.25rem' },
  id:        { fontFamily: 'monospace', color: t.textMuted },
  date:      { color: t.textMuted, fontSize: '0.875rem', margin: '0 0 1rem' },
  badge:     { display: 'inline-block', padding: '0.25rem 0.875rem', borderRadius: '9999px', fontSize: '0.8rem', fontWeight: 600, color: '#fff', marginBottom: '1.5rem' },
  section:   { background: t.surface, border: `1px solid ${t.border}`, borderRadius: '8px', padding: '1.25rem', marginBottom: '1rem' },
  row:       { display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '0.5rem 0', borderBottom: `1px solid ${t.border}` },
  label:     { color: t.textMuted, fontSize: '0.875rem' },
  value:     { fontWeight: 500, fontSize: '0.9rem', color: t.text },
}
