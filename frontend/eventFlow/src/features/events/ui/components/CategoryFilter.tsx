import type { Category } from '../../domain/entities/Category'

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
  chip: { padding: '0.4rem 1rem', borderRadius: '999px', border: '1px solid #cbd5e0', background: '#fff', cursor: 'pointer', fontSize: '0.875rem' },
  active: { background: '#3182ce', color: '#fff', borderColor: '#3182ce' },
}
