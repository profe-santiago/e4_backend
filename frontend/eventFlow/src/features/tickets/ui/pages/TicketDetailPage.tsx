import { useParams, useNavigate, Link } from 'react-router-dom'
import { useTicketDetail } from '../hooks/useTicketDetail'
import { QRDisplay } from '../components/QRDisplay'
import type { TicketStatus } from '../../domain/entities/Ticket'

const STATUS_CONFIG: Record<TicketStatus, { label: string; color: string }> = {
  ACTIVE:    { label: 'Activo',    color: '#38a169' },
  USED:      { label: 'Utilizado', color: '#718096' },
  CANCELLED: { label: 'Cancelado', color: '#e53e3e' },
}

const formatDate = (iso: string) =>
  new Date(iso).toLocaleDateString('es-AR', { day: '2-digit', month: 'long', year: 'numeric', hour: '2-digit', minute: '2-digit' })

export const TicketDetailPage = () => {
  const { id } = useParams<{ id: string }>()
  const navigate = useNavigate()
  const { data: ticket, isLoading, isError } = useTicketDetail(id ?? '')

  if (isLoading) return <div style={styles.feedback}>Cargando ticket...</div>
  if (isError || !ticket) return <div style={styles.error}>No se pudo cargar el ticket.</div>

  const statusConfig = STATUS_CONFIG[ticket.status]

  return (
    <div style={styles.container}>
      <button style={styles.back} onClick={() => navigate(-1)}>← Volver</button>

      <div style={styles.header}>
        <h1 style={styles.heading}>
          Ticket <span style={styles.id}>#{ticket.id.slice(0, 8).toUpperCase()}</span>
        </h1>
        <span style={{ ...styles.badge, background: statusConfig.color }}>
          {statusConfig.label}
        </span>
      </div>

      {ticket.status === 'ACTIVE' && (
        <div style={styles.qrSection}>
          <QRDisplay value={ticket.qrCode} size={240} />
        </div>
      )}

      {ticket.status === 'USED' && (
        <div style={styles.usedBanner}>
          ✓ Este ticket fue utilizado el {ticket.usedAt ? formatDate(ticket.usedAt) : '—'}
        </div>
      )}

      {ticket.status === 'CANCELLED' && (
        <div style={styles.cancelledBanner}>
          Este ticket fue cancelado.
        </div>
      )}

      <div style={styles.infoSection}>
        <div style={styles.infoRow}>
          <span style={styles.label}>Orden</span>
          <Link to={`/orders/${ticket.orderId}`} style={styles.link}>
            #{ticket.orderId.slice(0, 8).toUpperCase()}
          </Link>
        </div>
        <div style={styles.infoRow}>
          <span style={styles.label}>Ticket type</span>
          <span>#{ticket.ticketTypeId}</span>
        </div>
        <div style={styles.infoRow}>
          <span style={styles.label}>Comprado el</span>
          <span>{formatDate(ticket.purchasedAt)}</span>
        </div>
      </div>
    </div>
  )
}

const styles: Record<string, React.CSSProperties> = {
  container: { maxWidth: '560px', margin: '0 auto', padding: '2rem 1rem' },
  feedback: { textAlign: 'center', padding: '4rem', color: '#555' },
  error: { textAlign: 'center', padding: '4rem', color: '#e53e3e' },
  back: { background: 'none', border: 'none', cursor: 'pointer', color: '#3182ce', marginBottom: '1rem', padding: 0, fontSize: '0.9rem' },
  header: { display: 'flex', alignItems: 'center', gap: '1rem', marginBottom: '1.5rem', flexWrap: 'wrap' },
  heading: { fontSize: '1.4rem', fontWeight: 700, margin: 0 },
  id: { fontFamily: 'monospace', color: '#555' },
  badge: { padding: '0.3rem 0.9rem', borderRadius: '999px', color: '#fff', fontWeight: 600, fontSize: '0.85rem' },
  qrSection: { display: 'flex', justifyContent: 'center', padding: '2rem 0', marginBottom: '1rem' },
  usedBanner: { background: '#f0f4f8', border: '1px solid #cbd5e0', borderRadius: '8px', padding: '1rem', textAlign: 'center', color: '#555', marginBottom: '1.5rem' },
  cancelledBanner: { background: '#fff5f5', border: '1px solid #fed7d7', borderRadius: '8px', padding: '1rem', textAlign: 'center', color: '#c53030', marginBottom: '1.5rem' },
  infoSection: { border: '1px solid #e2e8f0', borderRadius: '8px', padding: '1.25rem', display: 'flex', flexDirection: 'column', gap: '0.75rem' },
  infoRow: { display: 'flex', justifyContent: 'space-between', fontSize: '0.9rem' },
  label: { color: '#718096', fontWeight: 500 },
  link: { color: '#3182ce', textDecoration: 'none', fontFamily: 'monospace' },
}
