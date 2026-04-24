import { useState } from 'react'
import { useValidateTicket } from '../hooks/useValidateTicket'
import type { Ticket } from '../../domain/entities/Ticket'

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
          <label htmlFor="qrCode">Código QR</label>
          <input
            id="qrCode"
            type="text"
            placeholder="Pegá el valor del QR acá"
            value={qrCode}
            onChange={(e) => setQrCode(e.target.value)}
            style={styles.input}
            autoFocus
          />
        </div>
        <button type="submit" disabled={isPending || !qrCode.trim()} style={styles.btn}>
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
              <span>#{lastValidated.ticketTypeId}</span>
            </div>
            <div style={styles.infoRow}>
              <span style={styles.label}>Comprado el</span>
              <span>{formatDate(lastValidated.purchasedAt)}</span>
            </div>
            <div style={styles.infoRow}>
              <span style={styles.label}>Estado</span>
              <span style={{ color: '#718096' }}>USED</span>
            </div>
          </div>
        </div>
      )}
    </div>
  )
}

const styles: Record<string, React.CSSProperties> = {
  container: { maxWidth: '560px', margin: '0 auto', padding: '2rem 1rem' },
  heading: { fontSize: '1.75rem', fontWeight: 700, marginBottom: '0.5rem' },
  subtitle: { color: '#555', marginBottom: '1.5rem' },
  form: { display: 'flex', flexDirection: 'column', gap: '1rem', marginBottom: '1.5rem' },
  field: { display: 'flex', flexDirection: 'column', gap: '0.25rem' },
  input: { padding: '0.6rem', fontSize: '1rem', borderRadius: '4px', border: '1px solid #cbd5e0', fontFamily: 'monospace' },
  btn: { padding: '0.75rem', fontSize: '1rem', background: '#3182ce', color: '#fff', border: 'none', borderRadius: '4px', cursor: 'pointer', fontWeight: 600 },
  errorBanner: { background: '#fff5f5', border: '1px solid #fed7d7', borderRadius: '8px', padding: '1rem', color: '#c53030', textAlign: 'center' },
  successBanner: { background: '#f0fff4', border: '1px solid #9ae6b4', borderRadius: '8px', padding: '1.25rem' },
  successTitle: { fontWeight: 700, color: '#276749', margin: '0 0 1rem', fontSize: '1.05rem' },
  ticketInfo: { display: 'flex', flexDirection: 'column', gap: '0.5rem' },
  infoRow: { display: 'flex', justifyContent: 'space-between', fontSize: '0.9rem' },
  label: { color: '#555', fontWeight: 500 },
  mono: { fontFamily: 'monospace' },
}
