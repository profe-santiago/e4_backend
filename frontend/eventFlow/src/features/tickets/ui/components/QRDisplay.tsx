import { QRCode } from 'react-qr-code'

interface Props {
  value: string
  size?: number
}

export const QRDisplay = ({ value, size = 220 }: Props) => (
  <div style={styles.qrWrapper}>
    <QRCode value={value} size={size} bgColor="#ffffff" fgColor="#0f172a" />
  </div>
)

const styles: Record<string, React.CSSProperties> = {
  qrWrapper: {
    padding: '1.25rem',
    background: '#ffffff',
    borderRadius: '12px',
    display: 'inline-flex',
    boxShadow: '0 0 0 1px rgba(0,0,0,0.08)',
  },
}
