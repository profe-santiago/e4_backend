import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { useCategories } from '../hooks/useCategories'
import { useCategoryActions } from '../hooks/useCategoryActions'
import type { Category, CreateCategoryRequest, UpdateCategoryRequest } from '../../domain/entities/Category'

const categorySchema = z.object({
  name: z.string().min(1, 'Requerido').max(100, 'Máximo 100 caracteres'),
  description: z.string().max(255, 'Máximo 255 caracteres').optional(),
})

type CategoryFormValues = z.infer<typeof categorySchema>

const CategoryForm = ({
  onSave,
  onCancel,
  initial,
  isPending,
}: {
  onSave: (values: CategoryFormValues) => void
  onCancel: () => void
  initial?: Partial<Category>
  isPending: boolean
}) => {
  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<CategoryFormValues>({
    resolver: zodResolver(categorySchema),
    defaultValues: {
      name: initial?.name ?? '',
      description: initial?.description ?? '',
    },
  })

  return (
    <form onSubmit={handleSubmit(onSave)} style={formStyles.form}>
      <div style={formStyles.row}>
        <div style={formStyles.field}>
          <label style={formStyles.label}>Nombre *</label>
          <input type="text" {...register('name')} style={formStyles.input} />
          {errors.name && <span style={formStyles.errorMsg}>{errors.name.message}</span>}
        </div>
        <div style={{ ...formStyles.field, flex: 2 }}>
          <label style={formStyles.label}>Descripción</label>
          <input type="text" {...register('description')} style={formStyles.input} placeholder="Opcional" />
          {errors.description && <span style={formStyles.errorMsg}>{errors.description.message}</span>}
        </div>
      </div>
      <div style={formStyles.actions}>
        <button type="button" style={formStyles.cancelBtn} onClick={onCancel}>
          Cancelar
        </button>
        <button type="submit" style={formStyles.saveBtn} disabled={isPending}>
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
    const request: CreateCategoryRequest = {
      name: values.name,
      description: values.description || undefined,
    }
    createCategory.mutate(request, {
      onSuccess: () => setShowCreateForm(false),
    })
  }

  const handleUpdate = (values: CategoryFormValues) => {
    if (editingId === null) return
    const request: UpdateCategoryRequest = {
      name: values.name,
      description: values.description || undefined,
    }
    updateCategory.mutate(
      { id: editingId, request },
      { onSuccess: () => setEditingId(null) },
    )
  }

  const handleConfirmDelete = () => {
    if (confirmDeleteId === null) return
    deleteCategory.mutate(confirmDeleteId, {
      onSuccess: () => setConfirmDeleteId(null),
    })
  }

  if (isLoading) return <div style={styles.feedback}>Cargando categorías...</div>
  if (isError) return <div style={styles.error}>Error al cargar las categorías.</div>

  return (
    <div style={styles.container}>
      <div style={styles.header}>
        <h1 style={styles.heading}>Gestión de categorías</h1>
        {!showCreateForm && (
          <button style={styles.createBtn} onClick={() => setShowCreateForm(true)}>
            + Nueva categoría
          </button>
        )}
      </div>

      {showCreateForm && (
        <div style={styles.formBox}>
          <h2 style={styles.formTitle}>Nueva categoría</h2>
          <CategoryForm
            onSave={handleCreate}
            onCancel={() => setShowCreateForm(false)}
            isPending={createCategory.isPending}
          />
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
                    {cat.description && (
                      <span style={styles.cardDesc}>{cat.description}</span>
                    )}
                  </div>
                  <div style={styles.cardActions}>
                    <button
                      style={styles.editBtn}
                      onClick={() => {
                        setShowCreateForm(false)
                        setEditingId(cat.id)
                      }}
                    >
                      Editar
                    </button>
                    <button
                      style={styles.deleteBtn}
                      onClick={() => setConfirmDeleteId(cat.id)}
                    >
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
              ¿Estás seguro que querés eliminar esta categoría? Esta acción{' '}
              <strong>no se puede deshacer</strong>.
            </p>
            <div style={styles.modalActions}>
              <button
                style={styles.cancelModalBtn}
                onClick={() => setConfirmDeleteId(null)}
              >
                Cancelar
              </button>
              <button
                style={styles.confirmDeleteBtn}
                disabled={deleteCategory.isPending}
                onClick={handleConfirmDelete}
              >
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
  container: { maxWidth: '800px', margin: '0 auto', padding: '2rem 1rem' },
  header: { display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.5rem' },
  heading: { fontSize: '1.75rem', fontWeight: 700 },
  createBtn: { padding: '0.6rem 1.25rem', background: '#3182ce', color: '#fff', border: 'none', borderRadius: '4px', cursor: 'pointer', fontWeight: 600, fontSize: '0.95rem' },
  formBox: { background: '#f7fafc', border: '1px solid #e2e8f0', borderRadius: '8px', padding: '1.25rem', marginBottom: '1.5rem' },
  formTitle: { fontSize: '1.1rem', fontWeight: 600, marginBottom: '1rem', color: '#2d3748' },
  list: { display: 'flex', flexDirection: 'column', gap: '0.75rem' },
  card: { background: '#fff', border: '1px solid #e2e8f0', borderRadius: '8px', padding: '1rem' },
  cardContent: { display: 'flex', justifyContent: 'space-between', alignItems: 'center', gap: '1rem' },
  cardInfo: { display: 'flex', flexDirection: 'column', gap: '0.2rem', flex: 1 },
  cardName: { fontWeight: 600, fontSize: '0.95rem', color: '#2d3748' },
  cardDesc: { color: '#718096', fontSize: '0.875rem' },
  cardActions: { display: 'flex', gap: '0.5rem', flexShrink: 0 },
  editBtn: { padding: '0.35rem 0.75rem', fontSize: '0.8rem', border: '1px solid #cbd5e0', borderRadius: '4px', background: '#fff', cursor: 'pointer', whiteSpace: 'nowrap' },
  deleteBtn: { padding: '0.35rem 0.75rem', fontSize: '0.8rem', border: '1px solid #fc8181', borderRadius: '4px', background: '#fff5f5', color: '#e53e3e', cursor: 'pointer', whiteSpace: 'nowrap' },
  empty: { textAlign: 'center', color: '#718096', marginTop: '4rem', fontSize: '1rem' },
  feedback: { textAlign: 'center', padding: '4rem', color: '#555' },
  error: { textAlign: 'center', padding: '4rem', color: '#e53e3e' },
  overlay: { position: 'fixed', inset: 0, background: 'rgba(0,0,0,0.5)', display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 1000 },
  modal: { background: '#fff', borderRadius: '8px', padding: '2rem', maxWidth: '420px', width: '90%' },
  modalText: { marginBottom: '1.5rem', color: '#333', lineHeight: 1.5 },
  modalActions: { display: 'flex', justifyContent: 'flex-end', gap: '0.75rem' },
  cancelModalBtn: { padding: '0.6rem 1.25rem', border: '1px solid #cbd5e0', borderRadius: '4px', background: '#fff', cursor: 'pointer' },
  confirmDeleteBtn: { padding: '0.6rem 1.25rem', background: '#e53e3e', color: '#fff', border: 'none', borderRadius: '4px', cursor: 'pointer', fontWeight: 600 },
}

const formStyles: Record<string, React.CSSProperties> = {
  form: { display: 'flex', flexDirection: 'column', gap: '0.75rem' },
  row: { display: 'flex', gap: '1rem' },
  field: { display: 'flex', flexDirection: 'column', gap: '0.2rem', flex: 1 },
  label: { fontWeight: 500, fontSize: '0.85rem', color: '#4a5568' },
  input: { padding: '0.5rem', fontSize: '0.9rem', borderRadius: '4px', border: '1px solid #cbd5e0' },
  errorMsg: { color: '#e53e3e', fontSize: '0.78rem' },
  actions: { display: 'flex', justifyContent: 'flex-end', gap: '0.5rem' },
  cancelBtn: { padding: '0.45rem 1rem', border: '1px solid #cbd5e0', borderRadius: '4px', background: '#fff', cursor: 'pointer', fontSize: '0.875rem' },
  saveBtn: { padding: '0.45rem 1rem', background: '#3182ce', color: '#fff', border: 'none', borderRadius: '4px', cursor: 'pointer', fontSize: '0.875rem', fontWeight: 600 },
}
