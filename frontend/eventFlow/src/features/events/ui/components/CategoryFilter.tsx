import type { Category } from '../../domain/entities/Category'
import { t } from '@/shared/config/theme'

interface Props {
  categories: Category[]
  selected: number | undefined
  onChange: (id: number | undefined) => void
}

export const CategoryFilter = ({ categories, selected, onChange }: Props) => (
  <div style={styles.container}>
    <button
      style={{ ...styles.chip, ...(selected === undefined ? styles.active : {}) }}
      onClick={() => onChange(undefined)}
    >
      Todos
    </button>
    {categories.map((cat) => (
      <button
        key={cat.id}
        style={{ ...styles.chip, ...(selected === cat.id ? styles.active : {}) }}
        onClick={() => onChange(cat.id)}
      >
        {cat.name}
      </button>
    ))}
  </div>
)

const styles: Record<string, React.CSSProperties> = {
  container: { display: 'flex', gap: '0.5rem', flexWrap: 'wrap', marginBottom: '1.5rem' },
  chip: {
    padding: '0.35rem 1rem',
    borderRadius: '999px',
    border: `1px solid ${t.border2}`,
    background: t.surface,
    color: t.textMuted,
    cursor: 'pointer',
    fontSize: '0.85rem',
    fontWeight: 500,
    transition: 'all 0.15s',
  },
  active: {
    background: t.accent,
    color: '#fff',
    borderColor: t.accent,
  },
}
