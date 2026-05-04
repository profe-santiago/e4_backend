import { useEffect, useState } from 'react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { useNavigate, useParams } from 'react-router-dom'
import { toast } from 'react-hot-toast'
import { useEventDetail } from '../hooks/useEventDetail'
import { useUpdateEvent } from '../hooks/useUpdateEvent'
import { useEventActions } from '../hooks/useEventActions'
import { useCategories } from '../hooks/useCategories'
import { useTicketTypesByEvent } from '../hooks/useTicketTypesByEvent'
import { useTicketTypeActions } from '../hooks/useTicketTypeActions'
import { useEventRepository } from '@/core/di/EventContext'
import type { EventStatus } from '../../domain/entities/Event'
import type { TicketType, CreateTicketTypeRequest } from '../../domain/entities/TicketType'
import { t } from '@/shared/config/theme'
import { COUNTRIES } from '@/shared/config/formOptions'
import { ImagePreview } from '@/shared/components/ImagePreview'

const isUrl = (s: string) => { try { return !!new URL(s) } catch { return false } }

const eventSchema = z.object({
  title:       z.string().min(1, 'Requerido').max(255),
  description: z.string().optional(),
  categoryId:  z.number().optional(),
  venue:       z.string().min(1, 'Requerido').max(255),
  city:        z.string().min(1, 'Requerido').max(100),
  country:     z.string().min(1, 'Requerido').max(100),
  startDate:   z.string().min(1, 'Requerido'),
  endDate:     z.string().optional(),
  imageUrl:    z.string().url('URL inválida').max(500).optional().or(z.literal('')),
}).superRefine((data, ctx) => {
  if (data.endDate && data.startDate && new Date(data.endDate) <= new Date(data.startDate)) {
    ctx.addIssue({
      code: z.ZodIssueCode.custom,
      message: 'Debe ser posterior a la fecha de inicio',
      path: ['endDate'],
    })
  }
})

const ticketSchema = z.object({
  name:           z.string().min(1, 'Requerido').max(100),
  description:    z.string().optional(),
  price:          z.number().min(0, 'No puede ser negativo'),
  totalQuantity:  z.number().int().min(1, 'Mínimo 1'),
  saleStartDate:  z.string().optional(),
  saleStartTime:  z.string().optional(),
  saleEndDate:    z.string().optional(),
  saleEndTime:    z.string().optional(),
})

type EventFormValues = z.infer<typeof eventSchema>
type TicketFormValues = z.infer<typeof ticketSchema>

const STATUS_LABEL: Record<EventStatus, string> = {
  DRAFT:     'Borrador',
  PUBLISHED: 'Publicado',
  CANCELLED: 'Cancelado',
}

const STATUS_COLOR: Record<EventStatus, string> = {
  DRAFT:     '#d69e2e',
  PUBLISHED: '#38a169',
  CANCELLED: '#718096',
}

const toDateInput = (isoStr: string): string => isoStr ? isoStr.split('T')[0] : ''
const toDatePart  = (isoStr?: string | null): string => isoStr ? isoStr.slice(0, 10) : ''
const toTimePart  = (isoStr?: string | null): string => isoStr ? isoStr.slice(11, 16) : ''
const combineDT   = (date?: string, time?: string, defaultTime = '00:00') =>
  date ? `${date}T${time || defaultTime}` : undefined

const TicketTypeForm = ({
  onSave, onCancel, initial, isPending,
}: {
  onSave: (values: TicketFormValues) => void
  onCancel: () => void
  initial?: Partial<TicketType>
  isPending: boolean
}) => {
  const { register, trigger, setError, getValues, formState: { errors } } = useForm<TicketFormValues>({
    resolver: zodResolver(ticketSchema),
    defaultValues: {
      name:          initial?.name ?? '',
      description:   initial?.description ?? '',
      price:         initial?.price ?? 0,
      totalQuantity: initial?.totalQuantity ?? 1,
      saleStartDate: toDatePart(initial?.saleStartDate),
      saleStartTime: toTimePart(initial?.saleStartDate) || '00:00',
      saleEndDate:   toDatePart(initial?.saleEndDate),
      saleEndTime:   toTimePart(initial?.saleEndDate) || '23:59',
    },
  })

  const handleClickGuardar = async () => {
    const isValid = await trigger()
    if (!isValid) return

    const values = getValues()
    const now = new Date()
    const toDT = (date?: string, time?: string) => new Date(`${date}T${time || '00:00'}`)

    if (values.saleStartDate && toDT(values.saleStartDate, values.saleStartTime) < now) {
      setError('saleStartDate', { message: 'No puede ser una fecha pasada' })
      return
    }
    if (values.saleEndDate && toDT(values.saleEndDate, values.saleEndTime) < now) {
      setError('saleEndDate', { message: 'No puede ser una fecha pasada' })
      return
    }
    if (values.saleStartDate && values.saleEndDate &&
        toDT(values.saleEndDate, values.saleEndTime) <= toDT(values.saleStartDate, values.saleStartTime)) {
      setError('saleEndDate', { message: 'Debe ser posterior al inicio de venta' })
      return
    }

    onSave(values)
  }

  return (
    <div style={ttStyles.form}>
      <div style={ttStyles.grid}>
        <div style={ttStyles.field}>
          <label className="ef-label">Nombre *</label>
          <input type="text" {...register('name')} className="ef-input" />
          {errors.name && <span className="ef-error">{errors.name.message}</span>}
        </div>
        <div style={ttStyles.field}>
          <label className="ef-label">Precio (USD) *</label>
          <input type="number" step="0.01" {...register('price', { valueAsNumber: true })} className="ef-input" />
          {errors.price && <span className="ef-error">{errors.price.message}</span>}
        </div>
        <div style={ttStyles.field}>
          <label className="ef-label">Cantidad *</label>
          <input type="number" {...register('totalQuantity', { valueAsNumber: true })} className="ef-input" />
          {errors.totalQuantity && <span className="ef-error">{errors.totalQuantity.message}</span>}
        </div>
      </div>
      <div style={ttStyles.field}>
        <label className="ef-label">Descripción</label>
        <input type="text" {...register('description')} className="ef-input" placeholder="Opcional" />
      </div>
      <div style={ttStyles.dateRow}>
        <div style={ttStyles.field}>
          <label className="ef-label">Inicio de venta</label>
          <div style={ttStyles.dateTimeRow}>
            <input type="date" {...register('saleStartDate')} className="ef-input" style={{ flex: 2 }} />
            <input type="time" {...register('saleStartTime')} className="ef-input" style={{ flex: 1 }} />
          </div>
          <span style={ttStyles.labelHint}>Hora · por defecto: inicio del día</span>
          {errors.saleStartDate && <span className="ef-error">{errors.saleStartDate.message}</span>}
        </div>
        <div style={ttStyles.field}>
          <label className="ef-label">Cierre de venta</label>
          <div style={ttStyles.dateTimeRow}>
            <input type="date" {...register('saleEndDate')} className="ef-input" style={{ flex: 2 }} />
            <input type="time" {...register('saleEndTime')} className="ef-input" style={{ flex: 1 }} />
          </div>
          <span style={ttStyles.labelHint}>Hora · por defecto: fin del día</span>
          {errors.saleEndDate && <span className="ef-error">{errors.saleEndDate.message}</span>}
        </div>
      </div>
      <p style={ttStyles.hint}>
        Si no configuras fechas de venta, la venta abrirá de inmediato y cerrará al inicio del evento.
      </p>
      <div style={ttStyles.actions}>
        <button type="button" className="ef-btn-ghost" style={ttStyles.btn} onClick={onCancel}>
          Cancelar
        </button>
        <button type="button" className="ef-btn" style={ttStyles.btn} disabled={isPending} onClick={handleClickGuardar}>
          {isPending ? 'Guardando...' : 'Guardar'}
        </button>
      </div>
    </div>
  )
}

export const EditEventPage = () => {
  const { id } = useParams<{ id: string }>()
  const navigate = useNavigate()
  const eventRepository = useEventRepository()
  const { data: event, isLoading, isError } = useEventDetail(id ?? '')
  const { data: categories = [] } = useCategories()
  const { mutate: update, isPending: isUpdating } = useUpdateEvent(id ?? '')
  const { changeStatus } = useEventActions()
  const { data: ticketTypes = [], isLoading: isLoadingTickets } = useTicketTypesByEvent(id ?? '')
  const { createTicketType, updateTicketType, deleteTicketType } = useTicketTypeActions(id ?? '')

  const [showTicketForm, setShowTicketForm] = useState(false)
  const [editingTicket, setEditingTicket] = useState<TicketType | null>(null)
  const [confirmDeleteTicketId, setConfirmDeleteTicketId] = useState<number | null>(null)
  const [uploading, setUploading] = useState(false)

  const { register, handleSubmit, reset, watch, setValue, formState: { errors, isDirty } } = useForm<EventFormValues>({
    resolver: zodResolver(eventSchema),
  })

  const imageUrl = watch('imageUrl') ?? ''

  const handleImageUpload = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0]
    if (!file) return
    if (!['image/jpeg', 'image/png', 'image/webp'].includes(file.type)) {
      toast.error('Formato no permitido. Usá JPEG, PNG o WEBP.')
      return
    }
    if (file.size > 5 * 1024 * 1024) {
      toast.error('La imagen supera el límite de 5 MB.')
      return
    }
    setUploading(true)
    try {
      const url = await eventRepository.uploadImage(file)
      setValue('imageUrl', url, { shouldValidate: true, shouldDirty: true })
    } catch {
      toast.error('Error al subir la imagen. Intentá de nuevo.')
    } finally {
      setUploading(false)
    }
  }

  useEffect(() => {
    if (event) {
      reset({
        title:       event.title,
        description: event.description ?? '',
        categoryId:  undefined,
        venue:       event.venue,
        city:        event.city,
        country:     event.country,
        startDate:   toDateInput(event.startDate),
        endDate:     event.endDate ? toDateInput(event.endDate) : '',
        imageUrl:    event.imageUrl ?? '',
      })
    }
  }, [event, reset])

  const onSubmit = (values: EventFormValues) => {
    update({
      title:       values.title,
      description: values.description || undefined,
      categoryId:  values.categoryId || undefined,
      venue:       values.venue,
      city:        values.city,
      country:     values.country,
      startDate:   new Date(values.startDate).toISOString(),
      endDate:     values.endDate ? new Date(values.endDate).toISOString() : undefined,
      imageUrl:    values.imageUrl || undefined,
    })
  }

  const handleSaveNewTicket = (values: TicketFormValues) => {
    const request: CreateTicketTypeRequest = {
      name:          values.name,
      description:   values.description || undefined,
      price:         values.price,
      currency:      'USD',
      totalQuantity: values.totalQuantity,
      saleStartDate: combineDT(values.saleStartDate, values.saleStartTime, '00:00'),
      saleEndDate:   combineDT(values.saleEndDate,   values.saleEndTime,   '23:59'),
    }
    createTicketType.mutate(request, { onSuccess: () => setShowTicketForm(false) })
  }

  const handleSaveEditTicket = (values: TicketFormValues) => {
    if (!editingTicket) return
    updateTicketType.mutate(
      {
        id: editingTicket.id,
        request: {
          name:          values.name,
          description:   values.description || undefined,
          price:         values.price,
          currency:      'USD',
          totalQuantity: values.totalQuantity,
          saleStartDate: combineDT(values.saleStartDate, values.saleStartTime, '00:00'),
          saleEndDate:   combineDT(values.saleEndDate,   values.saleEndTime,   '23:59'),
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
        <button style={styles.back} onClick={() => navigate('/my-events')}>← Mis eventos</button>
        <div style={styles.titleRow}>
          <h1 style={styles.heading}>Editar evento</h1>
          <span style={{ ...styles.badge, background: STATUS_COLOR[event.status] }}>
            {STATUS_LABEL[event.status]}
          </span>
        </div>
      </div>

      <div style={styles.statusActions}>
        {event.status === 'DRAFT' && (
          <button className="ef-btn" disabled={changeStatus.isPending} onClick={() => changeStatus.mutate({ id: event.id, status: 'PUBLISHED' })}>
            Publicar evento
          </button>
        )}
        {event.status === 'PUBLISHED' && (
          <button className="ef-btn-ghost" style={{ color: t.error, borderColor: t.error }} disabled={changeStatus.isPending} onClick={() => changeStatus.mutate({ id: event.id, status: 'CANCELLED' })}>
            Cancelar evento
          </button>
        )}
      </div>

      <form onSubmit={handleSubmit(onSubmit)} style={styles.form}>
        <div style={styles.field}>
          <label className="ef-label">Título *</label>
          <input type="text" {...register('title')} className="ef-input" />
          {errors.title && <span className="ef-error">{errors.title.message}</span>}
        </div>

        <div style={styles.field}>
          <label className="ef-label">Descripción</label>
          <textarea {...register('description')} className="ef-input" rows={4} style={{ resize: 'vertical' }} />
        </div>

        <div style={styles.field}>
          <label className="ef-label">Categoría</label>
          <select {...register('categoryId', { setValueAs: v => v === '' ? undefined : Number(v) })} className="ef-input">
            <option value="">Sin categoría</option>
            {categories.map((cat) => (
              <option key={cat.id} value={cat.id}>{cat.name}</option>
            ))}
          </select>
        </div>

        <div style={styles.field}>
          <label className="ef-label">Lugar *</label>
          <input type="text" {...register('venue')} className="ef-input" />
          {errors.venue && <span className="ef-error">{errors.venue.message}</span>}
        </div>

        <div style={styles.row}>
          <div style={styles.field}>
            <label className="ef-label">Ciudad *</label>
            <input type="text" {...register('city')} className="ef-input" />
            {errors.city && <span className="ef-error">{errors.city.message}</span>}
          </div>
          <div style={styles.field}>
            <label className="ef-label">País *</label>
            <input type="text" {...register('country')} className="ef-input" list="country-list-edit" />
            <datalist id="country-list-edit">
              {COUNTRIES.map((c) => <option key={c} value={c} />)}
            </datalist>
            {errors.country && <span className="ef-error">{errors.country.message}</span>}
          </div>
        </div>

        <div style={styles.row}>
          <div style={styles.field}>
            <label className="ef-label">Fecha de inicio *</label>
            <input type="date" {...register('startDate')} className="ef-input" />
            {errors.startDate && <span className="ef-error">{errors.startDate.message}</span>}
          </div>
          <div style={styles.field}>
            <label className="ef-label">Fecha de fin</label>
            <input type="date" {...register('endDate')} className="ef-input" />
            {errors.endDate && <span className="ef-error">{errors.endDate.message}</span>}
          </div>
        </div>

        <div style={styles.field}>
          <label className="ef-label">Imagen del evento</label>
          <input
            type="file"
            accept="image/jpeg,image/png,image/webp"
            className="ef-input"
            onChange={handleImageUpload}
            disabled={uploading}
            style={{ cursor: uploading ? 'not-allowed' : 'pointer' }}
          />
          <span style={styles.uploadHint}>
            {uploading ? 'Subiendo imagen...' : 'Formatos: JPEG, PNG, WEBP · Máximo 5 MB'}
          </span>
          {errors.imageUrl && <span className="ef-error">{errors.imageUrl.message}</span>}
          {isUrl(imageUrl) && <ImagePreview key={imageUrl} url={imageUrl} />}
        </div>

        <div style={styles.formActions}>
          <button type="button" className="ef-btn-ghost" onClick={() => navigate('/my-events')}>
            Cancelar
          </button>
          <button type="submit" className="ef-btn" disabled={isUpdating || !isDirty}>
            {isUpdating ? 'Guardando...' : 'Guardar cambios'}
          </button>
        </div>
      </form>

      <section style={styles.section}>
        <div style={styles.sectionHeader}>
          <h2 style={styles.sectionTitle}>Tipos de ticket</h2>
          {!showTicketForm && !editingTicket && (
            <button className="ef-btn" style={{ padding: '0.45rem 1rem', fontSize: '0.875rem' }} onClick={() => setShowTicketForm(true)}>
              + Agregar tipo
            </button>
          )}
        </div>

        {showTicketForm && (
          <div style={styles.ticketFormBox}>
            <TicketTypeForm onSave={handleSaveNewTicket} onCancel={() => setShowTicketForm(false)} isPending={createTicketType.isPending} />
          </div>
        )}

        {isLoadingTickets ? (
          <p style={styles.feedback}>Cargando tipos de ticket...</p>
        ) : ticketTypes.length === 0 && !showTicketForm ? (
          <p style={styles.empty}>Sin tipos de ticket. Agregá uno para que los compradores puedan adquirir entradas.</p>
        ) : (
          <div style={styles.ticketList}>
            {ticketTypes.map((tt) => (
              <div key={tt.id} style={styles.ticketCard}>
                {editingTicket?.id === tt.id ? (
                  <TicketTypeForm initial={tt} onSave={handleSaveEditTicket} onCancel={() => setEditingTicket(null)} isPending={updateTicketType.isPending} />
                ) : (
                  <>
                    <div style={styles.ticketInfo}>
                      <span style={styles.ticketName}>{tt.name}</span>
                      <span style={styles.ticketDetail}>
                        USD {tt.price.toFixed(2)} · {tt.availableQuantity}/{tt.totalQuantity} disponibles
                      </span>
                      {tt.description && <span style={styles.ticketDesc}>{tt.description}</span>}
                      {(tt.saleStartDate || tt.saleEndDate) && (
                        <span style={styles.ticketSaleDates}>
                          Venta:{' '}
                          {tt.saleStartDate
                            ? `desde ${new Date(tt.saleStartDate).toLocaleString('es-AR', { day: '2-digit', month: 'short', hour: '2-digit', minute: '2-digit' })}`
                            : 'ya abierta'
                          }
                          {tt.saleEndDate
                            ? ` · hasta ${new Date(tt.saleEndDate).toLocaleString('es-AR', { day: '2-digit', month: 'short', hour: '2-digit', minute: '2-digit' })}`
                            : ''
                          }
                        </span>
                      )}
                    </div>
                    <div style={styles.ticketActions}>
                      <button className="ef-btn-ghost" style={{ padding: '0.3rem 0.7rem', fontSize: '0.78rem' }} onClick={() => { setShowTicketForm(false); setEditingTicket(tt) }}>
                        Editar
                      </button>
                      <button className="ef-btn-danger" style={{ padding: '0.3rem 0.7rem', fontSize: '0.78rem' }} onClick={() => setConfirmDeleteTicketId(tt.id)}>
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

      {ticketTypes.length > 0 && !isLoadingTickets && (() => {
        const totalSold = ticketTypes.reduce((acc, tt) => acc + (tt.totalQuantity - tt.availableQuantity), 0)
        const totalRevenue = ticketTypes.reduce((acc, tt) => acc + (tt.totalQuantity - tt.availableQuantity) * tt.price, 0)
        const fmt = (n: number) => new Intl.NumberFormat('en-US', { style: 'currency', currency: 'USD' }).format(n)
        return (
          <section style={styles.section}>
            <h2 style={styles.sectionTitle}>Resumen de ventas</h2>
            <div style={salesStyles.grid}>
              {ticketTypes.map((tt) => {
                const sold = tt.totalQuantity - tt.availableQuantity
                return (
                  <div key={tt.id} style={salesStyles.card}>
                    <span style={salesStyles.cardLabel}>{tt.name}</span>
                    <span style={salesStyles.cardSold}>{sold} vendidos</span>
                    <span style={salesStyles.cardRevenue}>{fmt(sold * tt.price)}</span>
                    <div style={salesStyles.bar}>
                      <div style={{ ...salesStyles.barFill, width: `${tt.totalQuantity > 0 ? (sold / tt.totalQuantity) * 100 : 0}%` }} />
                    </div>
                    <span style={salesStyles.cardSub}>{tt.availableQuantity} disponibles de {tt.totalQuantity}</span>
                  </div>
                )
              })}
            </div>
            <div style={salesStyles.totals}>
              <span style={salesStyles.totalItem}>
                <span style={salesStyles.totalLabel}>Total vendidos</span>
                <strong style={salesStyles.totalValue}>{totalSold}</strong>
              </span>
              <span style={salesStyles.totalItem}>
                <span style={salesStyles.totalLabel}>Ingresos estimados</span>
                <strong style={{ ...salesStyles.totalValue, color: t.accent }}>{fmt(totalRevenue)}</strong>
              </span>
            </div>
          </section>
        )
      })()}

      {confirmDeleteTicketId !== null && (
        <div style={styles.overlay}>
          <div style={styles.modal}>
            <p style={styles.modalText}>
              ¿Eliminar este tipo de ticket? Esta acción <strong>no se puede deshacer</strong>.
            </p>
            <div style={styles.modalActions}>
              <button className="ef-btn-ghost" onClick={() => setConfirmDeleteTicketId(null)}>Cancelar</button>
              <button className="ef-btn-danger" disabled={deleteTicketType.isPending} onClick={() => {
                deleteTicketType.mutate(confirmDeleteTicketId, { onSuccess: () => setConfirmDeleteTicketId(null) })
              }}>
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
  container:      { maxWidth: '720px', margin: '0 auto' },
  feedback:       { color: t.textMuted, fontSize: '0.95rem' },
  error:          { textAlign: 'center', padding: '4rem', color: t.error },
  header:         { marginBottom: '1rem' },
  back:           { background: 'none', border: 'none', cursor: 'pointer', color: t.accent, fontSize: '0.9rem', padding: 0, marginBottom: '0.75rem', display: 'block', fontWeight: 500 },
  titleRow:       { display: 'flex', alignItems: 'center', gap: '0.75rem' },
  heading:        { fontSize: '1.75rem', fontWeight: 700, color: t.text },
  badge:          { display: 'inline-block', padding: '0.2rem 0.7rem', borderRadius: '9999px', fontSize: '0.8rem', fontWeight: 600, color: '#fff' },
  statusActions:  { marginBottom: '1.5rem', display: 'flex', gap: '0.75rem' },
  form:           { display: 'flex', flexDirection: 'column', gap: '1.25rem', marginBottom: '2.5rem' },
  row:            { display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' },
  field:          { display: 'flex', flexDirection: 'column', gap: '0.35rem' },
  formActions:    { display: 'flex', justifyContent: 'flex-end', gap: '0.75rem', marginTop: '0.5rem' },
  uploadHint:     { fontSize: '0.82rem', color: t.textDim },
  section:        { borderTop: `1px solid ${t.border}`, paddingTop: '1.75rem' },
  sectionHeader:  { display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1rem' },
  sectionTitle:   { fontSize: '1.15rem', fontWeight: 600, color: t.text },
  ticketFormBox:  { background: t.surface, border: `1px solid ${t.border}`, borderRadius: '8px', padding: '1.25rem', marginBottom: '1rem' },
  ticketList:     { display: 'flex', flexDirection: 'column', gap: '0.625rem' },
  ticketCard:     { background: t.surface, border: `1px solid ${t.border}`, borderRadius: '8px', padding: '1rem 1.25rem', display: 'flex', justifyContent: 'space-between', alignItems: 'center', gap: '1rem' },
  ticketInfo:     { display: 'flex', flexDirection: 'column', gap: '0.2rem', flex: 1 },
  ticketName:     { fontWeight: 600, fontSize: '0.95rem', color: t.text },
  ticketDetail:   { color: t.accent, fontSize: '0.85rem', fontWeight: 500 },
  ticketDesc:     { color: t.textMuted, fontSize: '0.82rem' },
  ticketSaleDates:{ color: t.textDim, fontSize: '0.78rem', fontStyle: 'italic' },
  ticketActions:  { display: 'flex', gap: '0.5rem', flexShrink: 0 },
  empty:          { color: t.textMuted, fontSize: '0.9rem' },
  overlay:        { position: 'fixed', inset: 0, background: 'rgba(0,0,0,0.65)', backdropFilter: 'blur(2px)', display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 1000 },
  modal:          { background: t.surface, border: `1px solid ${t.border}`, borderRadius: '12px', padding: '2rem', maxWidth: '420px', width: '90%' },
  modalText:      { marginBottom: '1.5rem', color: t.textMuted, lineHeight: 1.6 },
  modalActions:   { display: 'flex', justifyContent: 'flex-end', gap: '0.75rem' },
}

const ttStyles: Record<string, React.CSSProperties> = {
  form:      { display: 'flex', flexDirection: 'column', gap: '0.75rem', width: '100%' },
  labelHint: { fontWeight: 400, color: '#6b7280', fontSize: '0.75rem' },
  grid:    { display: 'grid', gridTemplateColumns: '2fr 1fr 1fr', gap: '0.75rem' },
  dateRow:     { display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '0.75rem' },
  dateTimeRow: { display: 'flex', gap: '0.5rem' },
  field:       { display: 'flex', flexDirection: 'column', gap: '0.35rem' },
  hint:    { margin: 0, fontSize: '0.78rem', color: t.textDim, lineHeight: 1.5, padding: '0.5rem 0.75rem', background: `${t.accent}0D`, borderRadius: '6px', borderLeft: `3px solid ${t.accent}40` },
  actions: { display: 'flex', justifyContent: 'flex-end', gap: '0.5rem' },
  btn:     { padding: '0.4rem 1rem', fontSize: '0.875rem' },
}

const salesStyles: Record<string, React.CSSProperties> = {
  grid:        { display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(180px, 1fr))', gap: '0.75rem', marginTop: '1rem', marginBottom: '1.25rem' },
  card:        { background: t.surface2, border: `1px solid ${t.border}`, borderRadius: '8px', padding: '1rem', display: 'flex', flexDirection: 'column', gap: '0.25rem' },
  cardLabel:   { fontWeight: 600, fontSize: '0.9rem', color: t.text },
  cardSold:    { fontSize: '1.35rem', fontWeight: 700, color: t.text },
  cardRevenue: { fontSize: '0.85rem', color: t.accent, fontWeight: 500 },
  bar:         { height: '4px', background: t.border, borderRadius: '2px', overflow: 'hidden', marginTop: '0.5rem' },
  barFill:     { height: '100%', background: t.accent, borderRadius: '2px', transition: 'width 0.3s ease' },
  cardSub:     { fontSize: '0.75rem', color: t.textDim },
  totals:      { display: 'flex', gap: '2rem', borderTop: `1px solid ${t.border}`, paddingTop: '1rem' },
  totalItem:   { display: 'flex', flexDirection: 'column', gap: '0.2rem' },
  totalLabel:  { fontSize: '0.78rem', color: t.textDim, textTransform: 'uppercase' as const, letterSpacing: '0.05em' },
  totalValue:  { fontSize: '1.15rem', fontWeight: 700, color: t.text },
}
