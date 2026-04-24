import type { OrderStatus } from '../../domain/entities/Order'

interface Props {
  status: OrderStatus
}

const STATUS_CONFIG: Record<OrderStatus, { label: string; color: string; description: string }> = {
  PENDING:   { label: 'Pendiente',   color: '#d69e2e', description: 'Reservando stock y procesando pago...' },
  CONFIRMED: { label: 'Confirmada',  color: '#38a169', description: 'Orden confirmada. Procesando pago.' },
  FAILED:    { label: 'Fallida',     color: '#e53e3e', description: 'La orden no pudo procesarse.' },
  CANCELLED: { label: 'Cancelada',   color: '#718096', description: 'La orden fue cancelada.' },
  REFUNDED:  { label: 'Reembolsada', color: '#3182ce', description: 'El reembolso fue procesado.' },
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
              <div style={{
                ...styles.dot,
                background: i <= currentStep ? '#3182ce' : '#e2e8f0',
              }} />
              <span style={{ color: i <= currentStep ? '#3182ce' : '#aaa', fontSize: '0.85rem' }}>
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
  container: { display: 'flex', flexDirection: 'column', alignItems: 'center', gap: '0.75rem', padding: '1.5rem', border: '1px solid #e2e8f0', borderRadius: '8px', marginBottom: '1.5rem' },
  steps: { display: 'flex', alignItems: 'center', gap: '0.5rem' },
  stepRow: { display: 'flex', alignItems: 'center', gap: '0.5rem' },
  dot: { width: '12px', height: '12px', borderRadius: '50%' },
  line: { width: '40px', height: '2px', background: '#e2e8f0' },
  badge: { padding: '0.3rem 1rem', borderRadius: '999px', color: '#fff', fontWeight: 600, fontSize: '0.9rem' },
  description: { color: '#555', margin: 0, fontSize: '0.9rem', textAlign: 'center' },
  polling: { color: '#888', fontSize: '0.8rem', margin: 0 },
}
