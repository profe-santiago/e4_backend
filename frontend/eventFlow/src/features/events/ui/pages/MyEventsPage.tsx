import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useMyEvents } from '../hooks/useMyEvents'
import { useEventActions } from '../hooks/useEventActions'
import type { Event, EventStatus } from '../../domain/entities/Event'
import { t } from '@/shared/config/theme'
import { formatDateShort } from '@/shared/utils/formatDate'

const statusLabel: Record<EventStatus, string> = {
  DRAFT:     'Borrador',
  PUBLISHED: 'Publicado',
  CANCELLED: 'Cancelado',
}

const statusColor: Record<EventStatus, string> = {
  DRAFT:     '#d69e2e',
  PUBLISHED: '#38a169',
  CANCELLED: '#718096',
}

export const MyEventsPage = () => {
  const navigate = useNavigate()
  const { data: events = [], isLoading, isError } = useMyEvents()
  const { changeStatus, deleteEvent } = useEventActions()
  const [confirmDeleteId, setConfirmDeleteId] = useState<string | null>(null)

  const handleChangeStatus = (event: Event) => {
    if (event.status === 'DRAFT') changeStatus.mutate({ id: event.id, status: 'PUBLISHED' })
    else if (event.status === 'PUBLISHED') changeStatus.mutate({ id: event.id, status: 'CANCELLED' })
  }

  const handleConfirmDelete = () => {
    if (!confirmDeleteId) return
    deleteEvent.mutate(confirmDeleteId, { onSuccess: () => setConfirmDeleteId(null) })
  }

  const formatDate = formatDateShort

  if (isLoading) return <div style={styles.feedback}>Cargando eventos...</div>
  if (isError) return <div style={styles.error}>Error al cargar los eventos.</div>

  return (
    <div style={styles.container}>
      <div style={styles.header}>
        <h1 style={styles.heading}>Mis eventos</h1>
        <button className="ef-btn" onClick={() => navigate('/events/new')}>
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
                    <span style={{ ...styles.badge, background: statusColor[event.status] }}>
                      {statusLabel[event.status]}
                    </span>
                  </td>
                  <td style={styles.td}>{formatDate(event.startDate)}</td>
                  <td style={styles.td}>{event.city}</td>
                  <td style={{ ...styles.td, display: 'flex', gap: '0.5rem', flexWrap: 'wrap' }}>
                    <button className="ef-btn-ghost" style={styles.actionBtn} onClick={() => navigate(`/events/${event.id}/overview`)}>
                      Ver
                    </button>
                    <button className="ef-btn-ghost" style={styles.actionBtn} onClick={() => navigate(`/events/${event.id}/edit`)}>
                      Editar
                    </button>
                    {event.status !== 'CANCELLED' && (
                      <button
                        className="ef-btn-ghost"
                        style={styles.actionBtn}
                        disabled={changeStatus.isPending}
                        onClick={() => handleChangeStatus(event)}
                      >
                        {event.status === 'DRAFT' ? 'Publicar' : 'Cancelar'}
                      </button>
                    )}
                    <button
                      className="ef-btn-danger"
                      style={styles.actionBtn}
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
              <button className="ef-btn-ghost" onClick={() => setConfirmDeleteId(null)}>
                Cancelar
              </button>
              <button className="ef-btn-danger" disabled={deleteEvent.isPending} onClick={handleConfirmDelete}>
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
  container:    { maxWidth: '1100px', margin: '0 auto' },
  header:       { display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.5rem' },
  heading:      { fontSize: '1.75rem', fontWeight: 700, color: t.text },
  tableWrapper: { overflowX: 'auto', background: t.surface, border: `1px solid ${t.border}`, borderRadius: '10px' },
  table:        { width: '100%', borderCollapse: 'collapse', fontSize: '0.9rem' },
  th:           { padding: '0.875rem 1rem', textAlign: 'left', borderBottom: `1px solid ${t.border}`, fontWeight: 600, color: t.textMuted, whiteSpace: 'nowrap', fontSize: '0.8rem', textTransform: 'uppercase', letterSpacing: '0.05em' },
  tr:           { borderBottom: `1px solid ${t.border}` },
  td:           { padding: '0.875rem 1rem', verticalAlign: 'middle', color: t.text },
  badge:        { display: 'inline-block', padding: '0.2rem 0.6rem', borderRadius: '9999px', fontSize: '0.75rem', fontWeight: 600, color: '#fff' },
  actionBtn:    { padding: '0.3rem 0.7rem', fontSize: '0.78rem' },
  empty:        { textAlign: 'center', color: t.textMuted, marginTop: '4rem', fontSize: '1rem' },
  feedback:     { textAlign: 'center', padding: '4rem', color: t.textMuted },
  error:        { textAlign: 'center', padding: '4rem', color: t.error },
  overlay:      { position: 'fixed', inset: 0, background: 'rgba(0,0,0,0.65)', backdropFilter: 'blur(2px)', display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 1000 },
  modal:        { background: t.surface, border: `1px solid ${t.border}`, borderRadius: '12px', padding: '2rem', maxWidth: '420px', width: '90%' },
  modalText:    { marginBottom: '1.5rem', color: t.textMuted, lineHeight: 1.6 },
  modalActions: { display: 'flex', justifyContent: 'flex-end', gap: '0.75rem' },
}
