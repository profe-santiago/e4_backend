import { Link } from 'react-router-dom'
import type { Event } from '../../domain/entities/Event'
import { t } from '@/shared/config/theme'
import { formatDate, toUtc } from '@/shared/utils/formatDate'

interface Props {
  event: Event
}

const getEventState = (startDate: string, endDate?: string) => {
  const now   = new Date()
  const start = toUtc(startDate)
  const end   = endDate ? toUtc(endDate) : start
  if (end < now)    return 'ended'
  if (start <= now) return 'ongoing'
  return 'upcoming'
}

const PILL: Record<string, { label: string; bg: string }> = {
  ongoing:  { label: 'En curso',    bg: t.warning },
  upcoming: { label: 'Próximo',     bg: t.accent  },
  ended:    { label: 'Finalizado',  bg: t.textDim },
}

export const EventCard = ({ event }: Props) => {
  const state = getEventState(event.startDate, event.endDate)
  const ended = state === 'ended'
  const pill  = PILL[state]

  return (
    <Link to={`/events/${event.id}`} className="ef-event-card" style={ended ? { opacity: 0.65 } : undefined}>

      {/* ── imagen ── */}
      <div style={s.imgWrap}>
        {event.imageUrl
          ? <img
              src={event.imageUrl}
              alt={event.title}
              style={{ ...s.img, ...(ended ? s.imgEnded : {}) }}
            />
          : <div style={s.noImg}>🎫</div>
        }
        <div style={s.imgOverlay} />

        {event.category?.name && (
          <span style={s.categoryPill}>{event.category.name}</span>
        )}
        <span style={{ ...s.statePill, background: pill.bg }}>{pill.label}</span>
      </div>

      {/* ── cuerpo ── */}
      <div style={s.body}>
        <h3 style={{ ...s.title, ...(ended ? { color: t.textMuted } : {}) }}>
          {event.title}
        </h3>

        <div style={s.meta}>
          <span style={s.metaRow}>
            <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"/><circle cx="12" cy="10" r="3"/></svg>
            {event.venue} · {event.city}
          </span>
          <span style={s.metaRow}>
            <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><rect x="3" y="4" width="18" height="18" rx="2" ry="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg>
            {formatDate(event.startDate)}
          </span>
        </div>

        <div style={s.footer}>
          <span style={{ ...s.cta, ...(ended ? { color: t.textDim } : {}) }}>
            {ended ? 'Evento finalizado' : 'Ver entradas →'}
          </span>
        </div>
      </div>
    </Link>
  )
}

const s: Record<string, React.CSSProperties> = {
  imgWrap:      { position: 'relative', height: '200px', background: t.surface2, overflow: 'hidden' },
  img:          { width: '100%', height: '100%', objectFit: 'cover', display: 'block', transition: 'transform 0.35s ease' },
  imgEnded:     { filter: 'grayscale(60%)', opacity: 0.6 },
  noImg:        { width: '100%', height: '100%', display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: '3rem', opacity: 0.25 },
  imgOverlay:   { position: 'absolute', inset: 0, background: 'linear-gradient(to top, rgba(11,23,32,0.55) 0%, transparent 55%)' },
  categoryPill: { position: 'absolute', top: '0.7rem', left: '0.7rem', background: 'rgba(11,23,32,0.72)', backdropFilter: 'blur(6px)', border: `1px solid ${t.border2}`, color: t.accent, fontSize: '0.62rem', fontWeight: 700, textTransform: 'uppercase' as const, letterSpacing: '0.09em', padding: '0.2rem 0.55rem', borderRadius: '999px' },
  statePill:    { position: 'absolute', top: '0.7rem', right: '0.7rem', color: '#fff', fontSize: '0.62rem', fontWeight: 700, letterSpacing: '0.05em', padding: '0.22rem 0.6rem', borderRadius: '999px', backdropFilter: 'blur(4px)' },
  body:         { padding: '1rem 1.1rem 1rem', display: 'flex', flexDirection: 'column', gap: '0.5rem', flex: 1 },
  title:        { margin: 0, fontSize: '1rem', fontWeight: 600, color: t.text, lineHeight: 1.35, display: '-webkit-box', WebkitLineClamp: 2, WebkitBoxOrient: 'vertical' as const, overflow: 'hidden' },
  meta:         { display: 'flex', flexDirection: 'column', gap: '0.3rem' },
  metaRow:      { display: 'flex', alignItems: 'center', gap: '0.4rem', fontSize: '0.78rem', color: t.textMuted },
  footer:       { marginTop: 'auto', paddingTop: '0.6rem', borderTop: `1px solid ${t.border}` },
  cta:          { fontSize: '0.82rem', fontWeight: 600, color: t.accent },
}
