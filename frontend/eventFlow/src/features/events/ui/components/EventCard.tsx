import { Link } from 'react-router-dom'
import type { Event } from '../../domain/entities/Event'
import { t } from '@/shared/config/theme'

interface Props {
  event: Event
}

const formatDate = (iso: string) =>
  new Date(iso).toLocaleDateString('es-AR', { day: '2-digit', month: 'short', year: 'numeric' })

const getEventState = (startDate: string, endDate?: string) => {
  const now   = new Date()
  const start = new Date(startDate)
  const end   = endDate ? new Date(endDate) : start
  if (end < now)    return 'ended'
  if (start <= now) return 'ongoing'
  return 'upcoming'
}

export const EventCard = ({ event }: Props) => {
  const state = getEventState(event.startDate, event.endDate)
  const ended = state === 'ended'

  return (
    <Link to={`/events/${event.id}`} className="ef-event-card" style={ended ? styles.cardEnded : undefined}>
      <div style={styles.imageWrap}>
        {event.imageUrl
          ? <img src={event.imageUrl} alt={event.title} style={{ ...styles.image, ...(ended ? styles.imageEnded : {}) }} />
          : <div style={styles.noImage}>🎫</div>
        }
      </div>
      <div style={styles.body}>
        {event.category?.name && (
          <span style={styles.category}>{event.category.name}</span>
        )}
        <h3 style={{ ...styles.title, ...(ended ? styles.titleEnded : {}) }}>{event.title}</h3>
        {state === 'ongoing' && <span style={styles.pill('ongoing')}>En curso</span>}
        {state === 'ended'   && <span style={styles.pill('ended')}>Finalizado</span>}
        <p style={styles.venue}>📍 {event.venue} — {event.city}</p>
        <p style={styles.date}>🗓 {formatDate(event.startDate)}</p>
      </div>
    </Link>
  )
}

const styles = {
  imageWrap:  { height: '160px', background: t.surface2, display: 'flex', alignItems: 'center', justifyContent: 'center', overflow: 'hidden', position: 'relative' } as React.CSSProperties,
  image:      { width: '100%', height: '100%', objectFit: 'cover' } as React.CSSProperties,
  imageEnded: { opacity: 0.45, filter: 'grayscale(60%)' } as React.CSSProperties,
  noImage:    { fontSize: '2.5rem', opacity: 0.4 } as React.CSSProperties,
  cardEnded:  { opacity: 0.75 } as React.CSSProperties,
  body:       { padding: '1rem 1.125rem 1.125rem' } as React.CSSProperties,
  category:   { fontSize: '0.7rem', color: t.accent, fontWeight: 700, textTransform: 'uppercase' as const, letterSpacing: '0.08em', display: 'block', marginBottom: '0.375rem' } as React.CSSProperties,
  title:      { margin: '0 0 0.5rem', fontSize: '0.975rem', fontWeight: 600, color: t.text, lineHeight: 1.35 } as React.CSSProperties,
  titleEnded: { color: t.textMuted } as React.CSSProperties,
  venue:      { margin: '0 0 0.25rem', fontSize: '0.8rem', color: t.textMuted } as React.CSSProperties,
  date:       { margin: 0, fontSize: '0.8rem', color: t.textDim } as React.CSSProperties,
  pill: (state: 'ongoing' | 'ended'): React.CSSProperties => ({
    display: 'inline-block',
    padding: '0.15rem 0.55rem',
    borderRadius: '9999px',
    fontSize: '0.68rem',
    fontWeight: 700,
    color: '#fff',
    background: state === 'ongoing' ? t.warning : t.textDim,
    letterSpacing: '0.04em',
    marginBottom: '0.4rem',
  }),
}
