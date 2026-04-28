import { Link } from 'react-router-dom'
import type { Event } from '../../domain/entities/Event'

interface Props {
  event: Event
}

const formatDate = (iso: string) =>
  new Date(iso).toLocaleDateString('es-AR', { day: '2-digit', month: 'short', year: 'numeric' })

export const EventCard = ({ event }: Props) => (
  <Link to={`/events/${event.id}`} style={styles.card}>
    <div style={styles.imagePlaceholder}>
      {event.imageUrl
        ? <img src={event.imageUrl} alt={event.title} style={styles.image} />
        : <span style={styles.noImage}>Sin imagen</span>
      }
    </div>
    <div style={styles.body}>
      <span style={styles.category}>{event.category?.name}</span>
      <h3 style={styles.title}>{event.title}</h3>
      <p style={styles.venue}>{event.venue} — {event.city}, {event.country}</p>
      <p style={styles.date}>{formatDate(event.startDate)}</p>
    </div>
  </Link>
)

const styles: Record<string, React.CSSProperties> = {
  card: { display: 'block', textDecoration: 'none', color: 'inherit', border: '1px solid #e2e8f0', borderRadius: '8px', overflow: 'hidden', background: '#fff', transition: 'box-shadow 0.2s' },
  imagePlaceholder: { height: '160px', background: '#edf2f7', display: 'flex', alignItems: 'center', justifyContent: 'center', overflow: 'hidden' },
  image: { width: '100%', height: '100%', objectFit: 'cover' },
  noImage: { fontSize: '2.5rem' },
  body: { padding: '1rem' },
  category: { fontSize: '0.75rem', color: '#3182ce', fontWeight: 600, textTransform: 'uppercase' },
  title: { margin: '0.25rem 0 0.5rem', fontSize: '1rem', fontWeight: 600 },
  venue: { margin: 0, fontSize: '0.875rem', color: '#555' },
  date: { margin: '0.25rem 0 0', fontSize: '0.875rem', color: '#888' },
}
