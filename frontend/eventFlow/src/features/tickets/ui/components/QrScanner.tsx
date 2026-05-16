import { useEffect, useRef } from 'react'
import { Html5Qrcode } from 'html5-qrcode'
import { t } from '@/shared/config/theme'

interface Props {
  onScan: (value: string) => void
  onClose: () => void
}

export const QrScanner = ({ onScan, onClose }: Props) => {
  const scannerRef   = useRef<Html5Qrcode | null>(null)
  const isRunning    = useRef(false)
  const hasScanned   = useRef(false)
  const onScanRef    = useRef(onScan)
  const containerId  = 'qr-reader'

  useEffect(() => { onScanRef.current = onScan }, [onScan])

  const stop = async () => {
    if (scannerRef.current && isRunning.current) {
      isRunning.current = false
      await scannerRef.current.stop().catch(() => {})
    }
  }

  useEffect(() => {
    const el = document.getElementById(containerId)
    if (el) el.innerHTML = ''

    const scanner = new Html5Qrcode(containerId)
    scannerRef.current = scanner
    hasScanned.current = false

    scanner.start(
      { facingMode: 'environment' },
      { fps: 10, qrbox: { width: 240, height: 240 } },
      (decoded) => {
        if (hasScanned.current) return
        hasScanned.current = true
        stop().then(() => onScanRef.current(decoded))
      },
      undefined,
    ).then(() => {
      isRunning.current = true
    }).catch(() => {})

    return () => { stop() }
  }, [])

  return (
    <div style={styles.overlay}>
      <div style={styles.modal}>
        <p style={styles.hint}>Apuntá la cámara al código QR del ticket</p>
        <div id={containerId} style={styles.video} />
        <button
          onClick={() => stop().then(onClose)}
          className="ef-btn"
          style={styles.closeBtn}
        >
          Cancelar
        </button>
      </div>
    </div>
  )
}

const styles: Record<string, React.CSSProperties> = {
  overlay:  { position: 'fixed', inset: 0, background: 'rgba(0,0,0,0.75)', zIndex: 1000, display: 'flex', alignItems: 'center', justifyContent: 'center', padding: '1rem' },
  modal:    { background: t.bg, borderRadius: '16px', padding: '1.5rem', width: '100%', maxWidth: '420px', display: 'flex', flexDirection: 'column', gap: '1rem', alignItems: 'center' },
  hint:     { color: t.textMuted, fontSize: '0.9rem', margin: 0, textAlign: 'center' },
  video:    { width: '100%', borderRadius: '8px', overflow: 'hidden' },
  closeBtn: { width: '100%', marginTop: '0.5rem' },
}
