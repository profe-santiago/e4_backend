import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { useNavigate } from 'react-router-dom'
import { useCreateEvent } from '../hooks/useCreateEvent'
import { useCategories } from '../hooks/useCategories'

const schema = z.object({
  title: z.string().min(1, 'Requerido').max(255),
  description: z.string().optional(),
  categoryId: z.coerce.number().optional(),
  venue: z.string().min(1, 'Requerido').max(255),
  city: z.string().min(1, 'Requerido').max(100),
  country: z.string().min(1, 'Requerido').max(100),
  startDate: z.string().min(1, 'Requerido'),
  endDate: z.string().optional(),
  imageUrl: z.string().url('URL inválida').max(500).optional().or(z.literal('')),
})

type FormValues = z.infer<typeof schema>

export const CreateEventPage = () => {
  const navigate = useNavigate()
  const { mutate: create, isPending } = useCreateEvent()
  const { data: categories = [] } = useCategories()

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<FormValues>({ resolver: zodResolver(schema) })

  const onSubmit = (values: FormValues) => {
    create({
      title: values.title,
      description: values.description || undefined,
      categoryId: values.categoryId || undefined,
      venue: values.venue,
      city: values.city,
      country: values.country,
      startDate: new Date(values.startDate).toISOString(),
      endDate: values.endDate ? new Date(values.endDate).toISOString() : undefined,
      imageUrl: values.imageUrl || undefined,
    })
  }

  return (
    <div style={styles.container}>
      <div style={styles.header}>
        <button style={styles.backBtn} onClick={() => navigate('/my-events')}>
          ← Mis eventos
        </button>
        <h1 style={styles.heading}>Crear evento</h1>
      </div>

      <form onSubmit={handleSubmit(onSubmit)} style={styles.form}>
        <div style={styles.field}>
          <label htmlFor="title" style={styles.label}>Título *</label>
          <input id="title" type="text" {...register('title')} style={styles.input} />
          {errors.title && <span style={styles.errorMsg}>{errors.title.message}</span>}
        </div>

        <div style={styles.field}>
          <label htmlFor="description" style={styles.label}>Descripción</label>
          <textarea id="description" {...register('description')} style={styles.textarea} rows={4} />
        </div>

        <div style={styles.field}>
          <label htmlFor="categoryId" style={styles.label}>Categoría</label>
          <select id="categoryId" {...register('categoryId')} style={styles.input}>
            <option value="">Sin categoría</option>
            {categories.map((cat) => (
              <option key={cat.id} value={cat.id}>
                {cat.name}
              </option>
            ))}
          </select>
        </div>

        <div style={styles.field}>
          <label htmlFor="venue" style={styles.label}>Lugar *</label>
          <input id="venue" type="text" {...register('venue')} style={styles.input} placeholder="Nombre del lugar" />
          {errors.venue && <span style={styles.errorMsg}>{errors.venue.message}</span>}
        </div>

        <div style={styles.row}>
          <div style={styles.field}>
            <label htmlFor="city" style={styles.label}>Ciudad *</label>
            <input id="city" type="text" {...register('city')} style={styles.input} />
            {errors.city && <span style={styles.errorMsg}>{errors.city.message}</span>}
          </div>
          <div style={styles.field}>
            <label htmlFor="country" style={styles.label}>País *</label>
            <input id="country" type="text" {...register('country')} style={styles.input} />
            {errors.country && <span style={styles.errorMsg}>{errors.country.message}</span>}
          </div>
        </div>

        <div style={styles.row}>
          <div style={styles.field}>
            <label htmlFor="startDate" style={styles.label}>Fecha de inicio *</label>
            <input id="startDate" type="date" {...register('startDate')} style={styles.input} />
            {errors.startDate && <span style={styles.errorMsg}>{errors.startDate.message}</span>}
          </div>
          <div style={styles.field}>
            <label htmlFor="endDate" style={styles.label}>Fecha de fin</label>
            <input id="endDate" type="date" {...register('endDate')} style={styles.input} />
          </div>
        </div>

        <div style={styles.field}>
          <label htmlFor="imageUrl" style={styles.label}>URL de imagen</label>
          <input id="imageUrl" type="url" {...register('imageUrl')} style={styles.input} placeholder="https://..." />
          {errors.imageUrl && <span style={styles.errorMsg}>{errors.imageUrl.message}</span>}
        </div>

        <div style={styles.formActions}>
          <button
            type="button"
            style={styles.cancelBtn}
            onClick={() => navigate('/my-events')}
          >
            Cancelar
          </button>
          <button type="submit" style={styles.submitBtn} disabled={isPending}>
            {isPending ? 'Creando...' : 'Crear evento'}
          </button>
        </div>
      </form>
    </div>
  )
}

const styles: Record<string, React.CSSProperties> = {
  container: { maxWidth: '720px', margin: '0 auto', padding: '2rem 1rem' },
  header: { marginBottom: '1.5rem' },
  backBtn: { background: 'none', border: 'none', cursor: 'pointer', color: '#3182ce', fontSize: '0.9rem', padding: 0, marginBottom: '0.75rem', display: 'block' },
  heading: { fontSize: '1.75rem', fontWeight: 700 },
  form: { display: 'flex', flexDirection: 'column', gap: '1.25rem' },
  row: { display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' },
  field: { display: 'flex', flexDirection: 'column', gap: '0.25rem' },
  label: { fontWeight: 500, fontSize: '0.9rem', color: '#4a5568' },
  input: { padding: '0.5rem', fontSize: '1rem', borderRadius: '4px', border: '1px solid #cbd5e0' },
  textarea: { padding: '0.5rem', fontSize: '1rem', borderRadius: '4px', border: '1px solid #cbd5e0', resize: 'vertical' },
  errorMsg: { color: '#e53e3e', fontSize: '0.82rem' },
  formActions: { display: 'flex', justifyContent: 'flex-end', gap: '0.75rem', marginTop: '0.5rem' },
  cancelBtn: { padding: '0.6rem 1.25rem', border: '1px solid #cbd5e0', borderRadius: '4px', background: '#fff', cursor: 'pointer' },
  submitBtn: { padding: '0.6rem 1.5rem', background: '#3182ce', color: '#fff', border: 'none', borderRadius: '4px', cursor: 'pointer', fontWeight: 600 },
}
