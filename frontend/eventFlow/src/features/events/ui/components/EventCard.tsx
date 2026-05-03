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
        {state === 'ongoing' && (
          <span style={styles.floatingPill('ongoing')}>En curso</span>
        )}
        {state === 'upcoming' && (
          <span style={styles.floatingPill('upcoming')}>Próximo</span>
        )}
        {state === 'ended' && (
          <span style={styles.floatingPill('ended')}>Finalizado</span>
        )}
      </div>
      <div style={styles.body}>
        {event.category?.name && (
          <span style={styles.category}>{event.category.name}</span>
        )}
        <h3 style={{ ...styles.title, ...(ended ? styles.titleEnded : {}) }}>{event.title}</h3>
        {event.description && (
          <p style={styles.description}>{event.description}</p>
        )}
        <div style={styles.meta}>
          <p style={styles.venue}>📍 {event.venue} — {event.city}</p>
          <p style={styles.date}>🗓 {formatDate(event.startDate)}</p>
        </div>
        <div style={styles.footer}>
          <span style={ended ? styles.footerLinkDim : styles.footerLink}>
            Ver entradas →
          </span>
        </div>
      </div>
    </Link>
  )
}

const styles = {
  imageWrap:  { height: '180px', background: t.surface2, display: 'flex', alignItems: 'center', justifyContent: 'center', overflow: 'hidden', position: 'relative' } as React.CSSProperties,
  image:      { width: '100%', height: '100%', objectFit: 'cover' } as React.CSSProperties,
  imageEnded: { opacity: 0.4, filter: 'grayscale(70%)' } as React.CSSProperties,
  noImage:    { fontSize: '2.75rem', opacity: 0.35 } as React.CSSProperties,
  cardEnded:  { opacity: 0.7 } as React.CSSProperties,
  body:       { padding: '1rem 1.125rem 1rem', display: 'flex', flexDirection: 'column', gap: '0.4rem', flex: 1 } as React.CSSProperties,
  category:   { fontSize: '0.68rem', color: t.accent, fontWeight: 700, textTransform: 'uppercase' as const, letterSpacing: '0.08em' } as React.CSSProperties,
  title:      { margin: 0, fontSize: '0.975rem', fontWeight: 600, color: t.text, lineHeight: 1.35 } as React.CSSProperties,
  titleEnded: { color: t.textMuted } as React.CSSProperties,
  description: {
    margin: 0,
    fontSize: '0.8rem',
    color: t.textDim,
    lineHeight: 1.5,
    display: '-webkit-box',
    WebkitLineClamp: 2,
    WebkitBoxOrient: 'vertical' as const,
    overflow: 'hidden',
  } as React.CSSProperties,
  meta:   { display: 'flex', flexDirection: 'column', gap: '0.2rem', marginTop: '0.25rem' } as React.CSSProperties,
  venue:  { margin: 0, fontSize: '0.78rem', color: t.textMuted } as React.CSSProperties,
  date:   { margin: 0, fontSize: '0.78rem', color: t.textDim } as React.CSSProperties,
  footer: { marginTop: 'auto', paddingTop: '0.75rem' } as React.CSSProperties,
  footerLink:    { fontSize: '0.8rem', color: t.accent, fontWeight: 600 } as React.CSSProperties,
  footerLinkDim: { fontSize: '0.8rem', color: t.textDim, fontWeight: 600 } as React.CSSProperties,
  floatingPill: (state: 'upcoming' | 'ongoing' | 'ended'): React.CSSProperties => ({
    position: 'absolute',
    top: '0.625rem',
    right: '0.625rem',
    padding: '0.2rem 0.6rem',
    borderRadius: '9999px',
    fontSize: '0.65rem',
    fontWeight: 700,
    letterSpacing: '0.05em',
    color: '#fff',
    background:
      state === 'ongoing'  ? t.warning :
      state === 'upcoming' ? t.accent  : t.textDim,
    backdropFilter: 'blur(4px)',
  }),
}
