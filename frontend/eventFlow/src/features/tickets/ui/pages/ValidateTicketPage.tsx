import { useState } from 'react'
import { useValidateTicket } from '../hooks/useValidateTicket'
import type { Ticket } from '../../domain/entities/Ticket'
import { t } from '@/shared/config/theme'

const formatDate = (iso: string) =>
  new Date(iso).toLocaleString('es-AR', { day: '2-digit', month: 'short', year: 'numeric', hour: '2-digit', minute: '2-digit' })

export const ValidateTicketPage = () => {
  const [qrCode, setQrCode] = useState('')
  const [lastValidated, setLastValidated] = useState<Ticket | null>(null)
  const { mutate: validate, isPending, isError, reset } = useValidateTicket()

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault()
    if (!qrCode.trim()) return
    reset()
    setLastValidated(null)
    validate(qrCode.trim(), {
      onSuccess: (ticket) => {
        setLastValidated(ticket)
        setQrCode('')
      },
    })
  }

  return (
    <div style={styles.container}>
      <h1 style={styles.heading}>Validar ticket</h1>
      <p style={styles.subtitle}>Ingresá el código QR del ticket para validarlo en la entrada.</p>

      <form onSubmit={handleSubmit} style={styles.form}>
        <div style={styles.field}>
          <label className="ef-label">Código QR</label>
          <input
            type="text"
            placeholder="Pegá el valor del QR acá"
            value={qrCode}
            onChange={(e) => setQrCode(e.target.value)}
            className="ef-input"
            style={{ fontFamily: 'monospace' }}
            autoFocus
          />
        </div>
        <button type="submit" disabled={isPending || !qrCode.trim()} className="ef-btn ef-btn-full">
          {isPending ? 'Validando...' : 'Validar ticket'}
        </button>
      </form>

      {isError && (
        <div style={styles.errorBanner}>
          ✗ Ticket inválido, ya utilizado o cancelado.
        </div>
      )}

      {lastValidated && (
        <div style={styles.successBanner}>
          <p style={styles.successTitle}>✓ Ticket válido — acceso permitido</p>
          <div style={styles.ticketInfo}>
            <div style={styles.infoRow}>
              <span style={styles.label}>Ticket ID</span>
              <span style={styles.mono}>{lastValidated.id.slice(0, 8).toUpperCase()}</span>
            </div>
            <div style={styles.infoRow}>
              <span style={styles.label}>Ticket type</span>
              <span style={{ color: t.text }}>#{lastValidated.ticketTypeId}</span>
            </div>
            <div style={styles.infoRow}>
              <span style={styles.label}>Comprado el</span>
              <span style={{ color: t.text }}>{formatDate(lastValidated.purchasedAt)}</span>
            </div>
            <div style={styles.infoRow}>
              <span style={styles.label}>Estado</span>
              <span style={{ color: t.textMuted }}>USED</span>
            </div>
          </div>
        </div>
      )}
    </div>
  )
}

const styles: Record<string, React.CSSProperties> = {
  container:    { maxWidth: '520px', margin: '0 auto' },
  heading:      { fontSize: '1.75rem', fontWeight: 700, color: t.text, marginBottom: '0.5rem' },
  subtitle:     { color: t.textMuted, marginBottom: '1.75rem', fontSize: '0.9rem' },
  form:         { display: 'flex', flexDirection: 'column', gap: '1rem', marginBottom: '1.5rem' },
  field:        { display: 'flex', flexDirection: 'column', gap: '0.35rem' },
  errorBanner:  { background: 'rgba(248,113,113,0.08)', border: `1px solid rgba(248,113,113,0.3)`, borderRadius: '8px', padding: '1rem', color: t.error, textAlign: 'center', fontWeight: 500 },
  successBanner:{ background: 'rgba(52,211,153,0.08)', border: `1px solid rgba(52,211,153,0.3)`, borderRadius: '8px', padding: '1.25rem' },
  successTitle: { fontWeight: 700, color: t.success, margin: '0 0 1rem', fontSize: '1.05rem' },
  ticketInfo:   { display: 'flex', flexDirection: 'column', gap: '0.5rem' },
  infoRow:      { display: 'flex', justifyContent: 'space-between', fontSize: '0.9rem', padding: '0.35rem 0', borderBottom: `1px solid ${t.border}` },
  label:        { color: t.textMuted, fontWeight: 500 },
  mono:         { fontFamily: 'monospace', color: t.text },
}
