import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { useCategories } from '../hooks/useCategories'
import { useCategoryActions } from '../hooks/useCategoryActions'
import type { Category, CreateCategoryRequest, UpdateCategoryRequest } from '../../domain/entities/Category'
import { t } from '@/shared/config/theme'

const categorySchema = z.object({
  name:        z.string().min(1, 'Requerido').max(100),
  description: z.string().max(255).optional(),
})

type CategoryFormValues = z.infer<typeof categorySchema>

const CategoryForm = ({
  onSave, onCancel, initial, isPending,
}: {
  onSave: (values: CategoryFormValues) => void
  onCancel: () => void
  initial?: Partial<Category>
  isPending: boolean
}) => {
  const { register, handleSubmit, formState: { errors } } = useForm<CategoryFormValues>({
    resolver: zodResolver(categorySchema),
    defaultValues: { name: initial?.name ?? '', description: initial?.description ?? '' },
  })

  return (
    <form onSubmit={handleSubmit(onSave)} style={formStyles.form}>
      <div style={formStyles.row}>
        <div style={formStyles.field}>
          <label className="ef-label">Nombre *</label>
          <input type="text" {...register('name')} className="ef-input" />
          {errors.name && <span className="ef-error">{errors.name.message}</span>}
        </div>
        <div style={{ ...formStyles.field, flex: 2 }}>
          <label className="ef-label">Descripción</label>
          <input type="text" {...register('description')} className="ef-input" placeholder="Opcional" />
        </div>
      </div>
      <div style={formStyles.actions}>
        <button type="button" className="ef-btn-ghost" style={{ padding: '0.45rem 1rem', fontSize: '0.875rem' }} onClick={onCancel}>
          Cancelar
        </button>
        <button type="submit" className="ef-btn" style={{ padding: '0.45rem 1rem', fontSize: '0.875rem' }} disabled={isPending}>
          {isPending ? 'Guardando...' : 'Guardar'}
        </button>
      </div>
    </form>
  )
}

export const AdminCategoriesPage = () => {
  const { data: categories = [], isLoading, isError } = useCategories()
  const { createCategory, updateCategory, deleteCategory } = useCategoryActions()
  const [showCreateForm, setShowCreateForm] = useState(false)
  const [editingId, setEditingId] = useState<number | null>(null)
  const [confirmDeleteId, setConfirmDeleteId] = useState<number | null>(null)

  const handleCreate = (values: CategoryFormValues) => {
    const request: CreateCategoryRequest = { name: values.name, description: values.description || undefined }
    createCategory.mutate(request, { onSuccess: () => setShowCreateForm(false) })
  }

  const handleUpdate = (values: CategoryFormValues) => {
    if (editingId === null) return
    const request: UpdateCategoryRequest = { name: values.name, description: values.description || undefined }
    updateCategory.mutate({ id: editingId, request }, { onSuccess: () => setEditingId(null) })
  }

  const handleConfirmDelete = () => {
    if (confirmDeleteId === null) return
    deleteCategory.mutate(confirmDeleteId, { onSuccess: () => setConfirmDeleteId(null) })
  }

  if (isLoading) return <div style={styles.feedback}>Cargando categorías...</div>
  if (isError) return <div style={styles.error}>Error al cargar las categorías.</div>

  return (
    <div style={styles.container}>
      <div style={styles.header}>
        <h1 style={styles.heading}>Gestión de categorías</h1>
        {!showCreateForm && (
          <button className="ef-btn" onClick={() => setShowCreateForm(true)}>
            + Nueva categoría
          </button>
        )}
      </div>

      {showCreateForm && (
        <div style={styles.formBox}>
          <h2 style={styles.formTitle}>Nueva categoría</h2>
          <CategoryForm onSave={handleCreate} onCancel={() => setShowCreateForm(false)} isPending={createCategory.isPending} />
        </div>
      )}

      {categories.length === 0 && !showCreateForm ? (
        <p style={styles.empty}>No hay categorías todavía.</p>
      ) : (
        <div style={styles.list}>
          {categories.map((cat) => (
            <div key={cat.id} style={styles.card}>
              {editingId === cat.id ? (
                <CategoryForm
                  initial={cat}
                  onSave={handleUpdate}
                  onCancel={() => setEditingId(null)}
                  isPending={updateCategory.isPending}
                />
              ) : (
                <div style={styles.cardContent}>
                  <div style={styles.cardInfo}>
                    <span style={styles.cardName}>{cat.name}</span>
                    {cat.description && <span style={styles.cardDesc}>{cat.description}</span>}
                  </div>
                  <div style={styles.cardActions}>
                    <button className="ef-btn-ghost" style={styles.actionBtn} onClick={() => { setShowCreateForm(false); setEditingId(cat.id) }}>
                      Editar
                    </button>
                    <button className="ef-btn-danger" style={styles.actionBtn} onClick={() => setConfirmDeleteId(cat.id)}>
                      Eliminar
                    </button>
                  </div>
                </div>
              )}
            </div>
          ))}
        </div>
      )}

      {confirmDeleteId !== null && (
        <div style={styles.overlay}>
          <div style={styles.modal}>
            <p style={styles.modalText}>
              ¿Estás seguro que quieres eliminar esta categoría? Esta acción{' '}
              <strong>no se puede deshacer</strong>.
            </p>
            <div style={styles.modalActions}>
              <button className="ef-btn-ghost" onClick={() => setConfirmDeleteId(null)}>Cancelar</button>
              <button className="ef-btn-danger" disabled={deleteCategory.isPending} onClick={handleConfirmDelete}>
                {deleteCategory.isPending ? 'Eliminando...' : 'Sí, eliminar'}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  )
}

const styles: Record<string, React.CSSProperties> = {
  container:    { maxWidth: '800px', margin: '0 auto' },
  header:       { display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.5rem' },
  heading:      { fontSize: '1.75rem', fontWeight: 700, color: t.text },
  formBox:      { background: t.surface2, border: `1px solid ${t.border}`, borderRadius: '10px', padding: '1.25rem', marginBottom: '1.5rem' },
  formTitle:    { fontSize: '1rem', fontWeight: 600, marginBottom: '1rem', color: t.text },
  list:         { display: 'flex', flexDirection: 'column', gap: '0.625rem' },
  card:         { background: t.surface, border: `1px solid ${t.border}`, borderRadius: '8px', padding: '1rem 1.25rem' },
  cardContent:  { display: 'flex', justifyContent: 'space-between', alignItems: 'center', gap: '1rem' },
  cardInfo:     { display: 'flex', flexDirection: 'column', gap: '0.2rem', flex: 1 },
  cardName:     { fontWeight: 600, fontSize: '0.95rem', color: t.text },
  cardDesc:     { color: t.textMuted, fontSize: '0.85rem' },
  cardActions:  { display: 'flex', gap: '0.5rem', flexShrink: 0 },
  actionBtn:    { padding: '0.3rem 0.7rem', fontSize: '0.78rem' },
  empty:        { textAlign: 'center', color: t.textMuted, marginTop: '4rem' },
  feedback:     { textAlign: 'center', padding: '4rem', color: t.textMuted },
  error:        { textAlign: 'center', padding: '4rem', color: t.error },
  overlay:      { position: 'fixed', inset: 0, background: 'rgba(0,0,0,0.65)', backdropFilter: 'blur(2px)', display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 1000 },
  modal:        { background: t.surface, border: `1px solid ${t.border}`, borderRadius: '12px', padding: '2rem', maxWidth: '420px', width: '90%' },
  modalText:    { marginBottom: '1.5rem', color: t.textMuted, lineHeight: 1.6 },
  modalActions: { display: 'flex', justifyContent: 'flex-end', gap: '0.75rem' },
}

const formStyles: Record<string, React.CSSProperties> = {
  form:    { display: 'flex', flexDirection: 'column', gap: '0.75rem' },
  row:     { display: 'flex', gap: '1rem' },
  field:   { display: 'flex', flexDirection: 'column', gap: '0.35rem', flex: 1 },
  actions: { display: 'flex', justifyContent: 'flex-end', gap: '0.5rem' },
}
