import { Link } from 'react-router-dom'
import { useMyOrders } from '../hooks/useMyOrders'
import { PaginationControl } from '@/features/events/ui/components/PaginationControl'
import type { OrderStatus } from '../../domain/entities/Order'
import { t } from '@/shared/config/theme'

const STATUS_COLORS: Record<OrderStatus, string> = {
  PENDING:   '#d69e2e',
  CONFIRMED: '#38a169',
  FAILED:    '#e53e3e',
  CANCELLED: '#718096',
  REFUNDED:  t.accent,
}

const STATUS_LABELS: Record<OrderStatus, string> = {
  PENDING:   'Pendiente',
  CONFIRMED: 'Confirmada',
  FAILED:    'Fallida',
  CANCELLED: 'Cancelada',
  REFUNDED:  'Reembolsada',
}

const formatDate = (iso: string) =>
  new Date(iso).toLocaleDateString('es-AR', { day: '2-digit', month: 'short', year: 'numeric' })

const formatPrice = (amount: number) =>
  new Intl.NumberFormat('en-US', { style: 'currency', currency: 'USD' }).format(amount)

export const MyOrdersPage = () => {
  const { data, isLoading, isError, page, onPageChange } = useMyOrders()

  return (
    <div style={styles.container}>
      <h1 style={styles.heading}>Mis órdenes</h1>

      {isLoading && <p style={styles.feedback}>Cargando órdenes...</p>}
      {isError && <p style={styles.error}>Error al cargar las órdenes.</p>}

      {data && (
        <>
          {data.content.length === 0
            ? <p style={styles.feedback}>Todavía no tenés órdenes.</p>
            : (
              <div style={styles.list}>
                {data.content.map((order) => (
                  <Link key={order.id} to={`/orders/${order.id}`} style={styles.card}>
                    <div>
                      <p style={styles.orderId}>#{order.id.slice(0, 8).toUpperCase()}</p>
                      <p style={styles.meta}>{formatDate(order.createdAt)}</p>
                      <p style={styles.meta}>{order.items.length} ítem(s)</p>
                    </div>
                    <div style={styles.right}>
                      <p style={styles.amount}>{formatPrice(order.totalAmount)}</p>
                      <span style={{ ...styles.badge, background: STATUS_COLORS[order.status] }}>
                        {STATUS_LABELS[order.status]}
                      </span>
                    </div>
                  </Link>
                ))}
              </div>
            )
          }
          <PaginationControl currentPage={page} totalPages={data.totalPages} onPageChange={onPageChange} />
        </>
      )}
    </div>
  )
}

const styles: Record<string, React.CSSProperties> = {
  container: { maxWidth: '800px', margin: '0 auto' },
  heading:   { fontSize: '1.75rem', fontWeight: 700, color: t.text, marginBottom: '1.5rem' },
  list:      { display: 'flex', flexDirection: 'column', gap: '0.75rem' },
  card:      { display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '1rem 1.25rem', background: t.surface, border: `1px solid ${t.border}`, borderRadius: '8px', textDecoration: 'none', color: 'inherit', transition: 'border-color 0.15s' },
  orderId:   { fontWeight: 600, margin: 0, fontFamily: 'monospace', color: t.text },
  meta:      { color: t.textMuted, fontSize: '0.85rem', margin: '0.2rem 0 0' },
  right:     { display: 'flex', flexDirection: 'column', alignItems: 'flex-end', gap: '0.4rem' },
  amount:    { fontWeight: 700, margin: 0, color: t.text },
  badge:     { padding: '0.2rem 0.75rem', borderRadius: '999px', color: '#fff', fontSize: '0.75rem', fontWeight: 600 },
  feedback:  { textAlign: 'center', color: t.textMuted, marginTop: '3rem' },
  error:     { textAlign: 'center', color: t.error, marginTop: '3rem' },
}
