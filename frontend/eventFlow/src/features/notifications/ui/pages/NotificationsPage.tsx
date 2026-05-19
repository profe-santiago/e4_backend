import { useEffect } from 'react'
import { useQueryClient } from '@tanstack/react-query'
import { useMyNotifications } from '../hooks/useMyNotifications'
import { markNotificationsViewed } from '../hooks/useUnreadNotificationCount'
import { PaginationControl } from '@/features/events/ui/components/PaginationControl'
import type { NotificationStatus, NotificationType } from '../../domain/entities/Notification'
import { t } from '@/shared/config/theme'
import { formatDateTime } from '@/shared/utils/formatDate'
import { useAuthStore } from '@/store/auth.store'

const TYPE_LABELS: Record<NotificationType, string> = {
  PURCHASE_CONFIRMATION: 'Compra confirmada',
  PAYMENT_CONFIRMED:     'Pago confirmado',
  EVENT_CANCELLED:       'Evento cancelado',
  REFUND_COMPLETED:      'Reembolso completado',
  REFUND_FAILED:         'Reembolso fallido',
  GENERAL:               'General',
}

const STATUS_COLORS: Record<NotificationStatus, string> = {
  PENDING: '#d69e2e',
  SENT:    '#38a169',
  FAILED:  '#e53e3e',
}

const formatDate = formatDateTime

export const NotificationsPage = () => {
  const { data, isLoading, isError, page, onPageChange } = useMyNotifications()
  const queryClient = useQueryClient()
  const userId = useAuthStore((s) => s.user?.userId)

  useEffect(() => {
    markNotificationsViewed()
    queryClient.invalidateQueries({ queryKey: ['notifications-unread', userId] })
  }, [])

  return (
    <div style={styles.container}>
      <h1 style={styles.heading}>Mis notificaciones</h1>

      {isLoading && <p style={styles.feedback}>Cargando notificaciones...</p>}
      {isError && <p style={styles.error}>Error al cargar las notificaciones.</p>}

      {data && (
        <>
          {data.content.length === 0
            ? <p style={styles.feedback}>No tienes notificaciones todavía.</p>
            : (
              <div style={styles.list}>
                {data.content.map((n) => (
                  <div key={n.id} style={styles.card}>
                    <div style={styles.cardHeader}>
                      <span style={styles.subject}>{n.subject}</span>
                      <span style={{ ...styles.badge, background: STATUS_COLORS[n.status] }}>
                        {n.status}
                      </span>
                    </div>
                    <p style={styles.type}>{TYPE_LABELS[n.type]}</p>
                    <p style={styles.message}>{n.message}</p>
                    <p style={styles.date}>
                      {n.sentAt ? formatDate(n.sentAt) : formatDate(n.createdAt)}
                    </p>
                  </div>
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
  container:  { maxWidth: '800px', margin: '0 auto' },
  heading:    { fontSize: '1.75rem', fontWeight: 700, color: t.text, marginBottom: '1.5rem' },
  list:       { display: 'flex', flexDirection: 'column', gap: '0.75rem' },
  card:       { padding: '1rem 1.25rem', border: `1px solid ${t.border}`, borderRadius: '8px', background: t.surface },
  cardHeader: { display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '0.25rem' },
  subject:    { fontWeight: 600, fontSize: '1rem', color: t.text },
  type:       { color: t.accent, fontSize: '0.8rem', margin: '0 0 0.5rem' },
  message:    { color: t.textMuted, fontSize: '0.9rem', margin: '0 0 0.75rem', lineHeight: 1.5 },
  date:       { color: t.textDim, fontSize: '0.78rem', margin: 0 },
  badge:      { padding: '0.2rem 0.75rem', borderRadius: '999px', color: '#fff', fontSize: '0.72rem', fontWeight: 600, flexShrink: 0 },
  feedback:   { textAlign: 'center', color: t.textMuted, marginTop: '3rem' },
  error:      { textAlign: 'center', color: t.error, marginTop: '3rem' },
}
