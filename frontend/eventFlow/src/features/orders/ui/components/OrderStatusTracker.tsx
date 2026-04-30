import type { OrderStatus } from '../../domain/entities/Order'
import { t } from '@/shared/config/theme'

interface Props {
  status: OrderStatus
}

const STATUS_CONFIG: Record<OrderStatus, { label: string; color: string; description: string }> = {
  PENDING:   { label: 'Pendiente',   color: '#d69e2e', description: 'Reservando stock y procesando pago...' },
  CONFIRMED: { label: 'Confirmada',  color: '#38a169', description: 'Orden confirmada. ¡El pago fue procesado!' },
  FAILED:    { label: 'Fallida',     color: '#e53e3e', description: 'La orden no pudo procesarse.' },
  CANCELLED: { label: 'Cancelada',   color: '#718096', description: 'La orden fue cancelada.' },
  REFUNDED:  { label: 'Reembolsada', color: t.accent,  description: 'El reembolso fue procesado.' },
}

const STEPS: OrderStatus[] = ['PENDING', 'CONFIRMED']

export const OrderStatusTracker = ({ status }: Props) => {
  const config = STATUS_CONFIG[status]
  const isTerminal = ['FAILED', 'CANCELLED', 'REFUNDED'].includes(status)
  const currentStep = STEPS.indexOf(status as typeof STEPS[number])

  return (
    <div style={styles.container}>
      {!isTerminal && (
        <div style={styles.steps}>
          {STEPS.map((step, i) => (
            <div key={step} style={styles.stepRow}>
              <div style={{ ...styles.dot, background: i <= currentStep ? t.accent : t.border2 }} />
              <span style={{ color: i <= currentStep ? t.accent : t.textDim, fontSize: '0.85rem' }}>
                {STATUS_CONFIG[step].label}
              </span>
              {i < STEPS.length - 1 && <div style={styles.line} />}
            </div>
          ))}
        </div>
      )}
      <div style={{ ...styles.badge, background: config.color }}>
        {config.label}
      </div>
      <p style={styles.description}>{config.description}</p>
      {status === 'PENDING' && (
        <p style={styles.polling}>Actualizando automáticamente...</p>
      )}
    </div>
  )
}

const styles: Record<string, React.CSSProperties> = {
  container: { display: 'flex', flexDirection: 'column', alignItems: 'center', gap: '0.75rem', padding: '1.5rem', background: t.surface, border: `1px solid ${t.border}`, borderRadius: '8px', marginBottom: '1.5rem' },
  steps: { display: 'flex', alignItems: 'center', gap: '0.5rem' },
  stepRow: { display: 'flex', alignItems: 'center', gap: '0.5rem' },
  dot: { width: '12px', height: '12px', borderRadius: '50%' },
  line: { width: '40px', height: '2px', background: t.border2 },
  badge: { padding: '0.3rem 1rem', borderRadius: '999px', color: '#fff', fontWeight: 600, fontSize: '0.9rem' },
  description: { color: t.textMuted, margin: 0, fontSize: '0.9rem', textAlign: 'center' },
  polling: { color: t.textDim, fontSize: '0.8rem', margin: 0 },
}
