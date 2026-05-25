import { useState } from 'react'
import { t } from '@/shared/config/theme'

export const ImagePreview = ({ url }: { url: string }) => {
  const [hasError, setHasError] = useState(false)

  if (hasError) {
    return (
      <div style={styles.error}>
        No se pudo cargar la imagen. Verifica la URL.
      </div>
    )
  }

  return (
    <img
      src={url}
      alt="Vista previa"
      onError={() => setHasError(true)}
      style={styles.img}
    />
  )
}

const styles: Record<string, React.CSSProperties> = {
  img:   { marginTop: '0.5rem', width: '100%', maxHeight: '220px', objectFit: 'cover', borderRadius: '8px', border: `1px solid ${t.border}` },
  error: { marginTop: '0.5rem', fontSize: '0.8rem', color: t.error, padding: '0.5rem 0.75rem', background: 'rgba(248,113,113,0.08)', borderRadius: '6px', border: `1px solid rgba(248,113,113,0.2)` },
}
