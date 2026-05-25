import { Link } from 'react-router-dom'
import { useMyTickets } from '../hooks/useMyTickets'
import { PaginationControl } from '@/features/events/ui/components/PaginationControl'
import type { TicketStatus } from '../../domain/entities/Ticket'
import { t } from '@/shared/config/theme'
import { formatDate } from '@/shared/utils/formatDate'

const STATUS_CONFIG: Record<TicketStatus, { label: string; color: string }> = {
  ACTIVE:    { label: 'Activo',    color: '#38a169' },
  USED:      { label: 'Utilizado', color: '#718096' },
  CANCELLED: { label: 'Cancelado', color: '#e53e3e' },
  EXPIRED:   { label: 'Vencido',   color: '#718096' },
}


export const MyTicketsPage = () => {
  const { data, isLoading, isError, page, onPageChange } = useMyTickets()

  return (
    <div style={styles.container}>
      <h1 style={styles.heading}>Mis tickets</h1>

      {isLoading && <p style={styles.feedback}>Cargando tickets...</p>}
      {isError && <p style={styles.error}>Error al cargar los tickets.</p>}

      {data && (
        <>
          {data.content.length === 0
            ? (
              <div style={styles.empty}>
                <p style={{ color: t.textMuted }}>Todavía no tienes tickets.</p>
                <Link to="/" className="ef-link">Ver eventos disponibles →</Link>
              </div>
            )
            : (
              <div style={styles.list}>
                {data.content.map((ticket) => {
                  const config = STATUS_CONFIG[ticket.status]
                  return (
                    <Link key={ticket.id} to={`/tickets/${ticket.id}`} style={styles.card}>
                      <div>
                        <p style={styles.ticketId}>#{ticket.id.slice(0, 8).toUpperCase()}</p>
                        <p style={styles.meta}>Orden #{ticket.orderId.slice(0, 8).toUpperCase()}</p>
                        <p style={styles.meta}>Comprado el {formatDate(ticket.purchasedAt)}</p>
                      </div>
                      <div style={styles.right}>
                        <span style={{ ...styles.badge, background: config.color }}>
                          {config.label}
                        </span>
                        {ticket.status === 'ACTIVE' && (
                          <span style={styles.qrHint}>Ver QR →</span>
                        )}
                      </div>
                    </Link>
                  )
                })}
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
  ticketId:  { fontWeight: 600, margin: 0, fontFamily: 'monospace', color: t.text },
  meta:      { color: t.textMuted, fontSize: '0.85rem', margin: '0.2rem 0 0' },
  right:     { display: 'flex', flexDirection: 'column', alignItems: 'flex-end', gap: '0.5rem' },
  badge:     { padding: '0.2rem 0.75rem', borderRadius: '999px', color: '#fff', fontSize: '0.75rem', fontWeight: 600 },
  qrHint:    { fontSize: '0.8rem', color: t.accent },
  empty:     { textAlign: 'center', padding: '3rem', display: 'flex', flexDirection: 'column', gap: '0.75rem', alignItems: 'center' },
  feedback:  { textAlign: 'center', color: t.textMuted, marginTop: '3rem' },
  error:     { textAlign: 'center', color: t.error, marginTop: '3rem' },
}
