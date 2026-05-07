import { useState, useCallback } from 'react'
import { useValidateTicket } from '../hooks/useValidateTicket'
import { useTicketTypesByEvent } from '@/features/events/ui/hooks/useTicketTypesByEvent'
import { QrScanner } from '../components/QrScanner'
import type { Ticket } from '../../domain/entities/Ticket'
import { t } from '@/shared/config/theme'

const formatDate = (iso: string) =>
  new Date(iso).toLocaleDateString('es-AR', {
    day: '2-digit', month: 'long', year: 'numeric',
    hour: '2-digit', minute: '2-digit',
  })

const InfoRow = ({ label, value }: { label: string; value: React.ReactNode }) => (
  <div style={styles.infoRow}>
    <span style={styles.label}>{label}</span>
    <span style={styles.value}>{value}</span>
  </div>
)

const ValidatedResult = ({ ticket }: { ticket: Ticket }) => {
  const { data: ticketTypes = [] } = useTicketTypesByEvent(ticket.eventId)
  const typeName = ticketTypes.find(tt => tt.id === ticket.ticketTypeId)?.name ?? `#${ticket.ticketTypeId}`

  return (
    <div style={styles.successBanner}>
      <p style={styles.successTitle}>✓ Ticket válido — acceso permitido</p>
      <div style={styles.ticketInfo}>
        <InfoRow label="Ticket ID"     value={<span style={styles.mono}>{ticket.id.toUpperCase()}</span>} />
        <InfoRow label="Orden"         value={<span style={styles.mono}>#{ticket.orderId.slice(0, 8).toUpperCase()}</span>} />
        <InfoRow label="Tipo de ticket" value={typeName} />
        <InfoRow label="Comprado el"   value={formatDate(ticket.purchasedAt)} />
        <InfoRow label="Estado"        value={<span style={{ color: '#718096' }}>USED</span>} />
      </div>
    </div>
  )
}

export const ValidateTicketPage = () => {
  const [qrCode, setQrCode] = useState('')
  const [scanning, setScanning] = useState(false)
  const [lastValidated, setLastValidated] = useState<Ticket | null>(null)
  const { mutate: validate, isPending, isError, reset } = useValidateTicket()

  const runValidation = useCallback((code: string) => {
    reset()
    setLastValidated(null)
    validate(code.trim(), {
      onSuccess: (ticket) => {
        setLastValidated(ticket)
        setQrCode('')
      },
    })
  }, [validate, reset])

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault()
    if (!qrCode.trim()) return
    runValidation(qrCode)
  }

  const handleScan = useCallback((value: string) => {
    setScanning(false)
    runValidation(value)
  }, [runValidation])

  return (
    <div style={styles.container}>
      <h1 style={styles.heading}>Validar ticket</h1>
      <p style={styles.subtitle}>Escaneá el QR del ticket o pegá el código manualmente.</p>

      <button
        onClick={() => setScanning(true)}
        className="ef-btn ef-btn-full"
        style={styles.scanBtn}
        type="button"
      >
        📷 Escanear con cámara
      </button>

      <div style={styles.divider}>
        <span style={styles.dividerText}>o ingresá el código manualmente</span>
      </div>

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

      {lastValidated && <ValidatedResult ticket={lastValidated} />}

      {scanning && <QrScanner onScan={handleScan} onClose={() => setScanning(false)} />}
    </div>
  )
}

const styles: Record<string, React.CSSProperties> = {
  container:    { maxWidth: '520px', margin: '0 auto' },
  heading:      { fontSize: '1.75rem', fontWeight: 700, color: t.text, marginBottom: '0.5rem' },
  subtitle:     { color: t.textMuted, marginBottom: '1.5rem', fontSize: '0.9rem' },
  scanBtn:      { marginBottom: '1.25rem' },
  divider:      { display: 'flex', alignItems: 'center', gap: '0.75rem', marginBottom: '1.25rem' },
  dividerText:  { color: t.textDim, fontSize: '0.8rem', flex: 1, textAlign: 'center' as const },
  form:         { display: 'flex', flexDirection: 'column', gap: '1rem', marginBottom: '1.5rem' },
  field:        { display: 'flex', flexDirection: 'column', gap: '0.35rem' },
  errorBanner:  { background: 'rgba(248,113,113,0.08)', border: `1px solid rgba(248,113,113,0.3)`, borderRadius: '8px', padding: '1rem', color: t.error, textAlign: 'center', fontWeight: 500 },
  successBanner:{ background: 'rgba(52,211,153,0.08)', border: `1px solid rgba(52,211,153,0.3)`, borderRadius: '8px', padding: '1.25rem' },
  successTitle: { fontWeight: 700, color: t.success, margin: '0 0 1rem', fontSize: '1.05rem' },
  ticketInfo:   { display: 'flex', flexDirection: 'column' },
  infoRow:      { display: 'flex', justifyContent: 'space-between', alignItems: 'center', fontSize: '0.9rem', padding: '0.5rem 0', borderBottom: `1px solid ${t.border}` },
  label:        { color: t.textMuted, fontWeight: 500 },
  value:        { color: t.text },
  mono:         { fontFamily: 'monospace', fontSize: '0.85rem' },
}
