import { useEffect, useState } from 'react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { useNavigate, useParams } from 'react-router-dom'
import { useEventDetail } from '../hooks/useEventDetail'
import { useUpdateEvent } from '../hooks/useUpdateEvent'
import { useEventActions } from '../hooks/useEventActions'
import { useCategories } from '../hooks/useCategories'
import { useTicketTypesByEvent } from '../hooks/useTicketTypesByEvent'
import { useTicketTypeActions } from '../hooks/useTicketTypeActions'
import type { EventStatus } from '../../domain/entities/Event'
import type { TicketType, CreateTicketTypeRequest } from '../../domain/entities/TicketType'

const eventSchema = z.object({
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

const ticketSchema = z.object({
  name: z.string().min(1, 'Requerido').max(100),
  description: z.string().optional(),
  price: z.coerce.number().min(0, 'No puede ser negativo'),
  currency: z.string().max(3).optional(),
  totalQuantity: z.coerce.number().int().min(1, 'Mínimo 1'),
})

type EventFormValues = z.infer<typeof eventSchema>
type TicketFormValues = z.infer<typeof ticketSchema>

const statusLabel: Record<EventStatus, string> = {
  DRAFT: 'Borrador',
  PUBLISHED: 'Publicado',
  CANCELLED: 'Cancelado',
}

const statusColor: Record<EventStatus, React.CSSProperties> = {
  DRAFT: { background: '#edf2f7', color: '#4a5568' },
  PUBLISHED: { background: '#c6f6d5', color: '#276749' },
  CANCELLED: { background: '#fed7d7', color: '#c53030' },
}

const toDateInput = (isoStr: string): string => {
  if (!isoStr) return ''
  return isoStr.split('T')[0]
}

const TicketTypeForm = ({
  onSave,
  onCancel,
  initial,
  isPending,
}: {
  onSave: (values: TicketFormValues) => void
  onCancel: () => void
  initial?: Partial<TicketType>
  isPending: boolean
}) => {
  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<TicketFormValues>({
    resolver: zodResolver(ticketSchema),
    defaultValues: {
      name: initial?.name ?? '',
      description: initial?.description ?? '',
      price: initial?.price ?? 0,
      currency: initial?.currency ?? 'USD',
      totalQuantity: initial?.totalQuantity ?? 1,
    },
  })

  return (
    <form onSubmit={handleSubmit(onSave)} style={ttStyles.form}>
      <div style={ttStyles.row}>
        <div style={ttStyles.field}>
          <label style={ttStyles.label}>Nombre *</label>
          <input type="text" {...register('name')} style={ttStyles.input} />
          {errors.name && <span style={ttStyles.errorMsg}>{errors.name.message}</span>}
        </div>
        <div style={ttStyles.field}>
          <label style={ttStyles.label}>Precio *</label>
          <input type="number" step="0.01" {...register('price')} style={ttStyles.input} />
          {errors.price && <span style={ttStyles.errorMsg}>{errors.price.message}</span>}
        </div>
        <div style={ttStyles.field}>
          <label style={ttStyles.label}>Moneda</label>
          <input type="text" maxLength={3} {...register('currency')} style={ttStyles.inputSm} placeholder="USD" />
        </div>
        <div style={ttStyles.field}>
          <label style={ttStyles.label}>Cantidad *</label>
          <input type="number" {...register('totalQuantity')} style={ttStyles.inputSm} />
          {errors.totalQuantity && <span style={ttStyles.errorMsg}>{errors.totalQuantity.message}</span>}
        </div>
      </div>
      <div style={ttStyles.field}>
        <label style={ttStyles.label}>Descripción</label>
        <input type="text" {...register('description')} style={ttStyles.input} />
      </div>
      <div style={ttStyles.formActions}>
        <button type="button" style={ttStyles.cancelBtn} onClick={onCancel}>Cancelar</button>
        <button type="submit" style={ttStyles.saveBtn} disabled={isPending}>
          {isPending ? 'Guardando...' : 'Guardar'}
        </button>
      </div>
    </form>
  )
}

export const EditEventPage = () => {
  const { id } = useParams<{ id: string }>()
  const navigate = useNavigate()
  const { data: event, isLoading, isError } = useEventDetail(id ?? '')
  const { data: categories = [] } = useCategories()
  const { mutate: update, isPending: isUpdating } = useUpdateEvent(id ?? '')
  const { changeStatus } = useEventActions()
  const { data: ticketTypes = [], isLoading: isLoadingTickets } = useTicketTypesByEvent(id ?? '')
  const { createTicketType, updateTicketType, deleteTicketType } = useTicketTypeActions(id ?? '')

  const [showTicketForm, setShowTicketForm] = useState(false)
  const [editingTicket, setEditingTicket] = useState<TicketType | null>(null)
  const [confirmDeleteTicketId, setConfirmDeleteTicketId] = useState<number | null>(null)

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors, isDirty },
  } = useForm<EventFormValues>({ resolver: zodResolver(eventSchema) })

  useEffect(() => {
    if (event) {
      reset({
        title: event.title,
        description: event.description ?? '',
        categoryId: undefined,
        venue: event.venue,
        city: event.city,
        country: event.country,
        startDate: toDateInput(event.startDate),
        endDate: event.endDate ? toDateInput(event.endDate) : '',
        imageUrl: event.imageUrl ?? '',
      })
    }
  }, [event, reset])

  const onSubmit = (values: EventFormValues) => {
    update({
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

  const handleSaveNewTicket = (values: TicketFormValues) => {
    const request: CreateTicketTypeRequest = {
      name: values.name,
      description: values.description || undefined,
      price: values.price,
      currency: values.currency || 'USD',
      totalQuantity: values.totalQuantity,
    }
    createTicketType.mutate(request, { onSuccess: () => setShowTicketForm(false) })
  }

  const handleSaveEditTicket = (values: TicketFormValues) => {
    if (!editingTicket) return
    updateTicketType.mutate(
      {
        id: editingTicket.id,
        request: {
          name: values.name,
          description: values.description || undefined,
          price: values.price,
          currency: values.currency || 'USD',
          totalQuantity: values.totalQuantity,
        },
      },
      { onSuccess: () => setEditingTicket(null) },
    )
  }

  if (isLoading) return <div style={styles.feedback}>Cargando evento...</div>
  if (isError || !event) return <div style={styles.error}>Error al cargar el evento.</div>

  return (
    <div style={styles.container}>
      <div style={styles.header}>
        <button style={styles.backBtn} onClick={() => navigate('/my-events')}>
          ← Mis eventos
        </button>
        <div style={styles.titleRow}>
          <h1 style={styles.heading}>Editar evento</h1>
          <span style={{ ...styles.badge, ...statusColor[event.status] }}>
            {statusLabel[event.status]}
          </span>
        </div>
      </div>

      <div style={styles.statusActions}>
        {event.status === 'DRAFT' && (
          <button
            style={styles.publishBtn}
            disabled={changeStatus.isPending}
            onClick={() => changeStatus.mutate({ id: event.id, status: 'PUBLISHED' })}
          >
            Publicar evento
          </button>
        )}
        {event.status === 'PUBLISHED' && (
          <button
            style={styles.cancelEventBtn}
            disabled={changeStatus.isPending}
            onClick={() => changeStatus.mutate({ id: event.id, status: 'CANCELLED' })}
          >
            Cancelar evento
          </button>
        )}
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
          <input id="venue" type="text" {...register('venue')} style={styles.input} />
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
          <button type="button" style={styles.cancelBtn} onClick={() => navigate('/my-events')}>
            Cancelar
          </button>
          <button type="submit" style={styles.submitBtn} disabled={isUpdating || !isDirty}>
            {isUpdating ? 'Guardando...' : 'Guardar cambios'}
          </button>
        </div>
      </form>

      <section style={styles.section}>
        <div style={styles.sectionHeader}>
          <h2 style={styles.sectionTitle}>Tipos de ticket</h2>
          {!showTicketForm && !editingTicket && (
            <button style={styles.addTicketBtn} onClick={() => setShowTicketForm(true)}>
              + Agregar tipo
            </button>
          )}
        </div>

        {showTicketForm && (
          <div style={styles.ticketFormBox}>
            <TicketTypeForm
              onSave={handleSaveNewTicket}
              onCancel={() => setShowTicketForm(false)}
              isPending={createTicketType.isPending}
            />
          </div>
        )}

        {isLoadingTickets ? (
          <p style={styles.feedback}>Cargando tipos de ticket...</p>
        ) : ticketTypes.length === 0 && !showTicketForm ? (
          <p style={styles.empty}>Sin tipos de ticket todavía.</p>
        ) : (
          <div style={styles.ticketList}>
            {ticketTypes.map((tt) => (
              <div key={tt.id} style={styles.ticketCard}>
                {editingTicket?.id === tt.id ? (
                  <TicketTypeForm
                    initial={tt}
                    onSave={handleSaveEditTicket}
                    onCancel={() => setEditingTicket(null)}
                    isPending={updateTicketType.isPending}
                  />
                ) : (
                  <>
                    <div style={styles.ticketInfo}>
                      <span style={styles.ticketName}>{tt.name}</span>
                      <span style={styles.ticketDetail}>
                        {tt.currency} {tt.price.toFixed(2)} · {tt.availableQuantity}/{tt.totalQuantity} disponibles
                      </span>
                      {tt.description && (
                        <span style={styles.ticketDesc}>{tt.description}</span>
                      )}
                    </div>
                    <div style={styles.ticketActions}>
                      <button
                        style={styles.ticketEditBtn}
                        onClick={() => {
                          setShowTicketForm(false)
                          setEditingTicket(tt)
                        }}
                      >
                        Editar
                      </button>
                      <button
                        style={styles.ticketDeleteBtn}
                        onClick={() => setConfirmDeleteTicketId(tt.id)}
                      >
                        Eliminar
                      </button>
                    </div>
                  </>
                )}
              </div>
            ))}
          </div>
        )}
      </section>

      {confirmDeleteTicketId !== null && (
        <div style={styles.overlay}>
          <div style={styles.modal}>
            <p style={styles.modalText}>
              ¿Eliminar este tipo de ticket? Esta acción <strong>no se puede deshacer</strong>.
            </p>
            <div style={styles.modalActions}>
              <button
                style={styles.cancelModalBtn}
                onClick={() => setConfirmDeleteTicketId(null)}
              >
                Cancelar
              </button>
              <button
                style={styles.confirmDeleteBtn}
                disabled={deleteTicketType.isPending}
                onClick={() => {
                  deleteTicketType.mutate(confirmDeleteTicketId, {
                    onSuccess: () => setConfirmDeleteTicketId(null),
                  })
                }}
              >
                {deleteTicketType.isPending ? 'Eliminando...' : 'Sí, eliminar'}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  )
}

const styles: Record<string, React.CSSProperties> = {
  container: { maxWidth: '720px', margin: '0 auto', padding: '2rem 1rem' },
  header: { marginBottom: '1rem' },
  backBtn: { background: 'none', border: 'none', cursor: 'pointer', color: '#3182ce', fontSize: '0.9rem', padding: 0, marginBottom: '0.75rem', display: 'block' },
  titleRow: { display: 'flex', alignItems: 'center', gap: '0.75rem' },
  heading: { fontSize: '1.75rem', fontWeight: 700 },
  badge: { display: 'inline-block', padding: '0.2rem 0.7rem', borderRadius: '9999px', fontSize: '0.8rem', fontWeight: 600 },
  statusActions: { marginBottom: '1.5rem', display: 'flex', gap: '0.75rem' },
  publishBtn: { padding: '0.55rem 1.25rem', background: '#ebf8ff', border: '1px solid #bee3f8', color: '#2b6cb0', borderRadius: '4px', cursor: 'pointer', fontWeight: 600 },
  cancelEventBtn: { padding: '0.55rem 1.25rem', background: '#fff5f5', border: '1px solid #fed7d7', color: '#c53030', borderRadius: '4px', cursor: 'pointer', fontWeight: 600 },
  form: { display: 'flex', flexDirection: 'column', gap: '1.25rem', marginBottom: '2.5rem' },
  row: { display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' },
  field: { display: 'flex', flexDirection: 'column', gap: '0.25rem' },
  label: { fontWeight: 500, fontSize: '0.9rem', color: '#4a5568' },
  input: { padding: '0.5rem', fontSize: '1rem', borderRadius: '4px', border: '1px solid #cbd5e0' },
  textarea: { padding: '0.5rem', fontSize: '1rem', borderRadius: '4px', border: '1px solid #cbd5e0', resize: 'vertical' },
  errorMsg: { color: '#e53e3e', fontSize: '0.82rem' },
  formActions: { display: 'flex', justifyContent: 'flex-end', gap: '0.75rem', marginTop: '0.5rem' },
  cancelBtn: { padding: '0.6rem 1.25rem', border: '1px solid #cbd5e0', borderRadius: '4px', background: '#fff', cursor: 'pointer' },
  submitBtn: { padding: '0.6rem 1.5rem', background: '#3182ce', color: '#fff', border: 'none', borderRadius: '4px', cursor: 'pointer', fontWeight: 600 },
  section: { borderTop: '2px solid #e2e8f0', paddingTop: '1.5rem' },
  sectionHeader: { display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1rem' },
  sectionTitle: { fontSize: '1.25rem', fontWeight: 600 },
  addTicketBtn: { padding: '0.45rem 1rem', background: '#3182ce', color: '#fff', border: 'none', borderRadius: '4px', cursor: 'pointer', fontSize: '0.875rem', fontWeight: 600 },
  ticketFormBox: { background: '#f7fafc', border: '1px solid #e2e8f0', borderRadius: '8px', padding: '1.25rem', marginBottom: '1rem' },
  ticketList: { display: 'flex', flexDirection: 'column', gap: '0.75rem' },
  ticketCard: { background: '#fff', border: '1px solid #e2e8f0', borderRadius: '8px', padding: '1rem' },
  ticketInfo: { display: 'flex', flexDirection: 'column', gap: '0.2rem', marginBottom: '0.75rem' },
  ticketName: { fontWeight: 600, fontSize: '0.95rem' },
  ticketDetail: { color: '#718096', fontSize: '0.875rem' },
  ticketDesc: { color: '#4a5568', fontSize: '0.82rem' },
  ticketActions: { display: 'flex', gap: '0.5rem' },
  ticketEditBtn: { padding: '0.3rem 0.75rem', fontSize: '0.8rem', border: '1px solid #cbd5e0', borderRadius: '4px', background: '#fff', cursor: 'pointer' },
  ticketDeleteBtn: { padding: '0.3rem 0.75rem', fontSize: '0.8rem', border: '1px solid #fc8181', borderRadius: '4px', background: '#fff5f5', color: '#e53e3e', cursor: 'pointer' },
  feedback: { color: '#555', fontSize: '0.95rem' },
  error: { textAlign: 'center', padding: '4rem', color: '#e53e3e' },
  empty: { color: '#718096', fontSize: '0.9rem' },
  overlay: { position: 'fixed', inset: 0, background: 'rgba(0,0,0,0.5)', display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 1000 },
  modal: { background: '#fff', borderRadius: '8px', padding: '2rem', maxWidth: '420px', width: '90%' },
  modalText: { marginBottom: '1.5rem', color: '#333', lineHeight: 1.5 },
  modalActions: { display: 'flex', justifyContent: 'flex-end', gap: '0.75rem' },
  cancelModalBtn: { padding: '0.6rem 1.25rem', border: '1px solid #cbd5e0', borderRadius: '4px', background: '#fff', cursor: 'pointer' },
  confirmDeleteBtn: { padding: '0.6rem 1.25rem', background: '#e53e3e', color: '#fff', border: 'none', borderRadius: '4px', cursor: 'pointer', fontWeight: 600 },
}

const ttStyles: Record<string, React.CSSProperties> = {
  form: { display: 'flex', flexDirection: 'column', gap: '0.75rem' },
  row: { display: 'grid', gridTemplateColumns: '2fr 1fr 0.6fr 0.8fr', gap: '0.75rem' },
  field: { display: 'flex', flexDirection: 'column', gap: '0.2rem' },
  label: { fontWeight: 500, fontSize: '0.82rem', color: '#4a5568' },
  input: { padding: '0.45rem', fontSize: '0.9rem', borderRadius: '4px', border: '1px solid #cbd5e0' },
  inputSm: { padding: '0.45rem', fontSize: '0.9rem', borderRadius: '4px', border: '1px solid #cbd5e0', width: '100%' },
  errorMsg: { color: '#e53e3e', fontSize: '0.75rem' },
  formActions: { display: 'flex', justifyContent: 'flex-end', gap: '0.5rem' },
  cancelBtn: { padding: '0.4rem 1rem', border: '1px solid #cbd5e0', borderRadius: '4px', background: '#fff', cursor: 'pointer', fontSize: '0.875rem' },
  saveBtn: { padding: '0.4rem 1rem', background: '#3182ce', color: '#fff', border: 'none', borderRadius: '4px', cursor: 'pointer', fontSize: '0.875rem', fontWeight: 600 },
}
