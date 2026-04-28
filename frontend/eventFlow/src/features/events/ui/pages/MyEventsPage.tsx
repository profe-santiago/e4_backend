import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useMyEvents } from '../hooks/useMyEvents'
import { useEventActions } from '../hooks/useEventActions'
import type { Event, EventStatus } from '../../domain/entities/Event'

const statusLabel: Record<EventStatus, string> = {
  DRAFT: 'Borrador',
  PUBLISHED: 'Publicado',
  CANCELLED: 'Cancelado',
}

const statusColor: Record<EventStatus, React.CSSProperties> = {
  DRAFT: { background: '#edf2f7', color: '#4a5568' },
  PUBLISHED: { background: '#c6f6d5', color: '#276749' },
  CANCELLED: { background: '#fed7d7', color: '#c53030' },
}

export const MyEventsPage = () => {
  const navigate = useNavigate()
  const { data: events = [], isLoading, isError } = useMyEvents()
  const { changeStatus, deleteEvent } = useEventActions()
  const [confirmDeleteId, setConfirmDeleteId] = useState<string | null>(null)

  const handleChangeStatus = (event: Event) => {
    if (event.status === 'DRAFT') {
      changeStatus.mutate({ id: event.id, status: 'PUBLISHED' })
    } else if (event.status === 'PUBLISHED') {
      changeStatus.mutate({ id: event.id, status: 'CANCELLED' })
    }
  }

  const handleConfirmDelete = () => {
    if (!confirmDeleteId) return
    deleteEvent.mutate(confirmDeleteId, {
      onSuccess: () => setConfirmDeleteId(null),
    })
  }

  const formatDate = (dateStr: string) =>
    new Date(dateStr).toLocaleDateString('es-AR', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
    })

  if (isLoading) return <div style={styles.feedback}>Cargando eventos...</div>
  if (isError) return <div style={styles.error}>Error al cargar los eventos.</div>

  return (
    <div style={styles.container}>
      <div style={styles.header}>
        <h1 style={styles.heading}>Mis eventos</h1>
        <button style={styles.createBtn} onClick={() => navigate('/events/new')}>
          + Crear evento
        </button>
      </div>

      {events.length === 0 ? (
        <p style={styles.empty}>No tenés eventos todavía. ¡Creá el primero!</p>
      ) : (
        <div style={styles.tableWrapper}>
          <table style={styles.table}>
            <thead>
              <tr>
                <th style={styles.th}>Título</th>
                <th style={styles.th}>Estado</th>
                <th style={styles.th}>Fecha inicio</th>
                <th style={styles.th}>Ciudad</th>
                <th style={styles.th}>Acciones</th>
              </tr>
            </thead>
            <tbody>
              {events.map((event) => (
                <tr key={event.id} style={styles.tr}>
                  <td style={styles.td}>{event.title}</td>
                  <td style={styles.td}>
                    <span style={{ ...styles.badge, ...statusColor[event.status] }}>
                      {statusLabel[event.status]}
                    </span>
                  </td>
                  <td style={styles.td}>{formatDate(event.startDate)}</td>
                  <td style={styles.td}>{event.city}</td>
                  <td style={styles.tdActions}>
                    <button
                      style={styles.actionBtn}
                      onClick={() => navigate(`/events/${event.id}/edit`)}
                    >
                      Editar
                    </button>
                    {event.status !== 'CANCELLED' && (
                      <button
                        style={{
                          ...styles.actionBtn,
                          ...(event.status === 'DRAFT' ? styles.publishBtn : styles.cancelBtn),
                        }}
                        disabled={changeStatus.isPending}
                        onClick={() => handleChangeStatus(event)}
                      >
                        {event.status === 'DRAFT' ? 'Publicar' : 'Cancelar'}
                      </button>
                    )}
                    <button
                      style={{ ...styles.actionBtn, ...styles.deleteBtn }}
                      onClick={() => setConfirmDeleteId(event.id)}
                    >
                      Eliminar
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {confirmDeleteId && (
        <div style={styles.overlay}>
          <div style={styles.modal}>
            <p style={styles.modalText}>
              ¿Estás seguro que querés eliminar este evento? Esta acción{' '}
              <strong>no se puede deshacer</strong>.
            </p>
            <div style={styles.modalActions}>
              <button
                style={styles.cancelModalBtn}
                onClick={() => setConfirmDeleteId(null)}
              >
                Cancelar
              </button>
              <button
                style={styles.confirmDeleteBtn}
                disabled={deleteEvent.isPending}
                onClick={handleConfirmDelete}
              >
                {deleteEvent.isPending ? 'Eliminando...' : 'Sí, eliminar'}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  )
}

const styles: Record<string, React.CSSProperties> = {
  container: { maxWidth: '1100px', margin: '0 auto', padding: '2rem 1rem' },
  header: { display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.5rem' },
  heading: { fontSize: '1.75rem', fontWeight: 700 },
  createBtn: { padding: '0.6rem 1.25rem', background: '#3182ce', color: '#fff', border: 'none', borderRadius: '4px', cursor: 'pointer', fontWeight: 600, fontSize: '0.95rem' },
  tableWrapper: { overflowX: 'auto' },
  table: { width: '100%', borderCollapse: 'collapse', fontSize: '0.95rem' },
  th: { padding: '0.75rem 1rem', textAlign: 'left', borderBottom: '2px solid #e2e8f0', fontWeight: 600, color: '#4a5568', whiteSpace: 'nowrap' },
  tr: { borderBottom: '1px solid #e2e8f0' },
  td: { padding: '0.75rem 1rem', verticalAlign: 'middle' },
  tdActions: { padding: '0.75rem 1rem', verticalAlign: 'middle', display: 'flex', gap: '0.5rem', flexWrap: 'wrap' },
  badge: { display: 'inline-block', padding: '0.2rem 0.6rem', borderRadius: '9999px', fontSize: '0.75rem', fontWeight: 600 },
  actionBtn: { padding: '0.35rem 0.75rem', fontSize: '0.8rem', border: '1px solid #cbd5e0', borderRadius: '4px', background: '#fff', cursor: 'pointer', whiteSpace: 'nowrap' },
  publishBtn: { background: '#ebf8ff', borderColor: '#bee3f8', color: '#2b6cb0' },
  cancelBtn: { background: '#fff5f5', borderColor: '#fed7d7', color: '#c53030' },
  deleteBtn: { background: '#fff5f5', borderColor: '#fc8181', color: '#e53e3e' },
  empty: { textAlign: 'center', color: '#718096', marginTop: '4rem', fontSize: '1rem' },
  feedback: { textAlign: 'center', padding: '4rem', color: '#555' },
  error: { textAlign: 'center', padding: '4rem', color: '#e53e3e' },
  overlay: { position: 'fixed', inset: 0, background: 'rgba(0,0,0,0.5)', display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 1000 },
  modal: { background: '#fff', borderRadius: '8px', padding: '2rem', maxWidth: '420px', width: '90%' },
  modalText: { marginBottom: '1.5rem', color: '#333', lineHeight: 1.5 },
  modalActions: { display: 'flex', justifyContent: 'flex-end', gap: '0.75rem' },
  cancelModalBtn: { padding: '0.6rem 1.25rem', border: '1px solid #cbd5e0', borderRadius: '4px', background: '#fff', cursor: 'pointer' },
  confirmDeleteBtn: { padding: '0.6rem 1.25rem', background: '#e53e3e', color: '#fff', border: 'none', borderRadius: '4px', cursor: 'pointer', fontWeight: 600 },
}
