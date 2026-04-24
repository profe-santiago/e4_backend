import { Link } from 'react-router-dom'
import { useMyOrders } from '../hooks/useMyOrders'
import { PaginationControl } from '@/features/events/ui/components/PaginationControl'
import type { OrderStatus } from '../../domain/entities/Order'

const STATUS_COLORS: Record<OrderStatus, string> = {
  PENDING:   '#d69e2e',
  CONFIRMED: '#38a169',
  FAILED:    '#e53e3e',
  CANCELLED: '#718096',
  REFUNDED:  '#3182ce',
}

const formatDate = (iso: string) =>
  new Date(iso).toLocaleDateString('es-AR', { day: '2-digit', month: 'short', year: 'numeric' })

const formatPrice = (amount: number) =>
  new Intl.NumberFormat('es-AR', { style: 'currency', currency: 'ARS' }).format(amount)

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
                      <p style={styles.orderId}>Orden #{order.id.slice(0, 8).toUpperCase()}</p>
                      <p style={styles.date}>{formatDate(order.createdAt)}</p>
                      <p style={styles.items}>{order.items.length} ítem(s)</p>
                    </div>
                    <div style={styles.right}>
                      <p style={styles.amount}>{formatPrice(order.totalAmount)}</p>
                      <span style={{ ...styles.badge, background: STATUS_COLORS[order.status] }}>
                        {order.status}
                      </span>
                    </div>
                  </Link>
                ))}
              </div>
            )
          }
          <PaginationControl
            currentPage={page}
            totalPages={data.totalPages}
            onPageChange={onPageChange}
          />
        </>
      )}
    </div>
  )
}

const styles: Record<string, React.CSSProperties> = {
  container: { maxWidth: '800px', margin: '0 auto', padding: '2rem 1rem' },
  heading: { fontSize: '1.75rem', fontWeight: 700, marginBottom: '1.5rem' },
  list: { display: 'flex', flexDirection: 'column', gap: '0.75rem' },
  card: { display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '1rem 1.25rem', border: '1px solid #e2e8f0', borderRadius: '8px', background: '#fff', textDecoration: 'none', color: 'inherit' },
  orderId: { fontWeight: 600, margin: 0, fontFamily: 'monospace' },
  date: { color: '#888', fontSize: '0.85rem', margin: '0.2rem 0 0' },
  items: { color: '#555', fontSize: '0.85rem', margin: '0.2rem 0 0' },
  right: { display: 'flex', flexDirection: 'column', alignItems: 'flex-end', gap: '0.4rem' },
  amount: { fontWeight: 700, margin: 0 },
  badge: { padding: '0.2rem 0.75rem', borderRadius: '999px', color: '#fff', fontSize: '0.75rem', fontWeight: 600 },
  feedback: { textAlign: 'center', color: '#555', marginTop: '3rem' },
  error: { textAlign: 'center', color: '#e53e3e', marginTop: '3rem' },
}
