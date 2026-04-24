import QRCode from 'react-qr-code'

interface Props {
  value: string
  size?: number
}

export const QRDisplay = ({ value, size = 220 }: Props) => (
  <div style={styles.container}>
    <div style={styles.qrWrapper}>
      <QRCode value={value} size={size} />
    </div>
    <p style={styles.hint}>Presentá este código en la entrada</p>
  </div>
)

const styles: Record<string, React.CSSProperties> = {
  container: { display: 'flex', flexDirection: 'column', alignItems: 'center', gap: '0.75rem' },
  qrWrapper: { padding: '1.25rem', background: '#fff', borderRadius: '8px', border: '1px solid #e2e8f0', display: 'inline-block' },
  hint: { color: '#718096', fontSize: '0.85rem', margin: 0 },
}
