import { useParams, useNavigate, Link } from 'react-router-dom'
import { useTicketDetail } from '../hooks/useTicketDetail'
import { useTicketTypesByEvent } from '@/features/events/ui/hooks/useTicketTypesByEvent'
import { QRDisplay } from '../components/QRDisplay'
import type { TicketStatus } from '../../domain/entities/Ticket'
import { t } from '@/shared/config/theme'

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
  const { data: ticketTypes = [] } = useTicketTypesByEvent(ticket?.eventId ?? '')
  const ticketTypeName = ticketTypes.find(tt => tt.id === ticket?.ticketTypeId)?.name

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
          <div style={styles.qrCard}>
            <p style={styles.qrInstructionText}>Presentá este código en la entrada</p>
            <QRDisplay value={ticket.qrCode} size={220} />
            <div style={styles.qrFooter}>
              <span style={styles.qrTicketId}>{ticket.id.toUpperCase()}</span>
              <span style={styles.qrTicketIdLabel}>ID del ticket</span>
            </div>
          </div>
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
          <Link to={`/orders/${ticket.orderId}`} className="ef-link" style={{ fontFamily: 'monospace', fontSize: '0.9rem' }}>
            #{ticket.orderId.slice(0, 8).toUpperCase()}
          </Link>
        </div>
        <div style={styles.infoRow}>
          <span style={styles.label}>Tipo de ticket</span>
          <span style={styles.value}>{ticketTypeName ?? `#${ticket.ticketTypeId}`}</span>
        </div>
        <div style={styles.infoRow}>
          <span style={styles.label}>Comprado el</span>
          <span style={styles.value}>{formatDate(ticket.purchasedAt)}</span>
        </div>
      </div>
    </div>
  )
}

const styles: Record<string, React.CSSProperties> = {
  container:      { maxWidth: '520px', margin: '0 auto' },
  feedback:       { textAlign: 'center', padding: '4rem', color: t.textMuted },
  error:          { textAlign: 'center', padding: '4rem', color: t.error },
  back:           { background: 'none', border: 'none', cursor: 'pointer', color: t.accent, marginBottom: '1rem', padding: 0, fontSize: '0.9rem', fontWeight: 500 },
  header:         { display: 'flex', alignItems: 'center', gap: '1rem', marginBottom: '1.5rem', flexWrap: 'wrap' },
  heading:        { fontSize: '1.4rem', fontWeight: 700, color: t.text, margin: 0 },
  id:             { fontFamily: 'monospace', color: t.textMuted },
  badge:          { padding: '0.3rem 0.9rem', borderRadius: '999px', color: '#fff', fontWeight: 600, fontSize: '0.85rem', flexShrink: 0 },
  qrSection:           { display: 'flex', justifyContent: 'center', marginBottom: '1.75rem' },
  qrCard:              { background: t.surface, border: `1px solid ${t.border}`, borderRadius: '16px', padding: '1.75rem 2rem', display: 'flex', flexDirection: 'column', alignItems: 'center', gap: '1.25rem', width: '100%' },
  qrInstructionText:   { color: t.textMuted, fontSize: '0.875rem', fontWeight: 500, margin: 0 },
  qrFooter:            { display: 'flex', flexDirection: 'column', alignItems: 'center', gap: '0.2rem', borderTop: `1px solid ${t.border}`, paddingTop: '1rem', width: '100%' },
  qrTicketId:          { fontFamily: 'monospace', fontSize: '0.78rem', color: t.textDim, letterSpacing: '0.08em', wordBreak: 'break-all' as const, textAlign: 'center' as const },
  qrTicketIdLabel:     { fontSize: '0.68rem', color: t.textDim, textTransform: 'uppercase' as const, letterSpacing: '0.1em' },
  usedBanner:     { background: t.surface2, border: `1px solid ${t.border2}`, borderRadius: '8px', padding: '1rem', textAlign: 'center', color: t.textMuted, marginBottom: '1.5rem' },
  cancelledBanner:{ background: 'rgba(248,113,113,0.08)', border: `1px solid rgba(248,113,113,0.3)`, borderRadius: '8px', padding: '1rem', textAlign: 'center', color: t.error, marginBottom: '1.5rem' },
  infoSection:    { background: t.surface, border: `1px solid ${t.border}`, borderRadius: '8px', padding: '1.25rem', display: 'flex', flexDirection: 'column', gap: '0.75rem' },
  infoRow:        { display: 'flex', justifyContent: 'space-between', fontSize: '0.9rem' },
  label:          { color: t.textMuted, fontWeight: 500 },
  value:          { color: t.text },
}
