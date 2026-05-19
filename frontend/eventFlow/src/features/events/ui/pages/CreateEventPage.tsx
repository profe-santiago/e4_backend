import { useState, useRef } from 'react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { useNavigate } from 'react-router-dom'
import { useMutation, useQueryClient } from '@tanstack/react-query'
import { toast } from 'react-hot-toast'
import { useEventRepository } from '@/core/di/EventContext'
import { useTicketTypeRepository } from '@/core/di/TicketTypeContext'
import { CreateEventUseCase } from '../../application/use-cases/CreateEventUseCase'
import { CreateTicketTypeUseCase } from '../../application/use-cases/CreateTicketTypeUseCase'
import { useCategories } from '../hooks/useCategories'
import type { CreateTicketTypeRequest } from '../../domain/entities/TicketType'
import { t } from '@/shared/config/theme'
import { COUNTRIES } from '@/shared/config/formOptions'
import { ImagePreview } from '@/shared/components/ImagePreview'
import { formatDateTimeShort } from '@/shared/utils/formatDate'

// ── Helpers ───────────────────────────────────────────────────────────────────

const isUrl = (s: string) => { try { return !!new URL(s) } catch { return false } }
const todayStr = () => new Date().toISOString().split('T')[0]

// ── Schemas ──────────────────────────────────────────────────────────────────

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
  const today = new Date(); today.setHours(0, 0, 0, 0)
  if (data.startDate && new Date(data.startDate + 'T00:00') < today) {
    ctx.addIssue({ code: z.ZodIssueCode.custom, message: 'No puede ser una fecha pasada', path: ['startDate'] })
  }
  if (data.endDate && data.startDate && new Date(data.endDate + 'T00:00') < new Date(data.startDate + 'T00:00')) {
    ctx.addIssue({ code: z.ZodIssueCode.custom, message: 'Debe ser igual o posterior a la fecha de inicio', path: ['endDate'] })
  }
})

const ticketSchema = z.object({
  name:          z.string().min(1, 'Requerido').max(100),
  description:   z.string().optional(),
  price:         z.number().min(0, 'No puede ser negativo'),
  totalQuantity: z.number().int().min(1, 'Mínimo 1'),
  saleStartDate: z.string().optional(),
  saleStartTime: z.string().optional(),
  saleEndDate:   z.string().optional(),
  saleEndTime:   z.string().optional(),
})

const combineDT = (date?: string, time?: string, defaultTime = '00:00') =>
  date ? `${date}T${time || defaultTime}` : undefined

type EventFormValues = z.infer<typeof eventSchema>
type TicketFormValues = z.infer<typeof ticketSchema>

// ── Ticket type form (NOT a <form> to avoid nesting inside the event form) ───

const TicketTypeForm = ({ onAdd, onCancel }: { onAdd: (v: TicketFormValues) => void; onCancel: () => void }) => {
  const { register, trigger, setError, getValues, reset, formState: { errors } } = useForm<TicketFormValues>({
    resolver: zodResolver(ticketSchema),
    defaultValues: { totalQuantity: 1, price: 0, saleStartTime: '00:00', saleEndTime: '23:59' },
  })

  const handleClickAgregar = async () => {
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

    onAdd(values)
    reset({ totalQuantity: 1, price: 0, saleStartTime: '00:00', saleEndTime: '23:59' })
  }

  return (
    <div style={ttStyles.form}>
      <div style={ttStyles.grid}>
        <div style={ttStyles.field}>
          <label className="ef-label">Nombre *</label>
          <input type="text" {...register('name')} className="ef-input" placeholder="Ej: General, VIP..." />
          {errors.name && <span className="ef-error">{errors.name.message}</span>}
        </div>
        <div style={ttStyles.field}>
          <label className="ef-label">Precio (USD) *</label>
          <input type="number" step="0.01" min="0" {...register('price', { valueAsNumber: true })} className="ef-input" />
          {errors.price && <span className="ef-error">{errors.price.message}</span>}
        </div>
        <div style={ttStyles.field}>
          <label className="ef-label">Cantidad *</label>
          <input type="number" min="1" {...register('totalQuantity', { valueAsNumber: true })} className="ef-input" />
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
        <button type="button" className="ef-btn" style={ttStyles.btn} onClick={handleClickAgregar}>
          Agregar tipo
        </button>
      </div>
    </div>
  )
}

// ── Main page ────────────────────────────────────────────────────────────────

export const CreateEventPage = () => {
  const navigate = useNavigate()
  const queryClient = useQueryClient()
  const eventRepository = useEventRepository()
  const ticketTypeRepository = useTicketTypeRepository()
  const { data: categories = [] } = useCategories()

  const [ticketTypes, setTicketTypes] = useState<TicketFormValues[]>([])
  const [showTicketForm, setShowTicketForm] = useState(false)

  const { register, handleSubmit, watch, setValue, formState: { errors } } = useForm<EventFormValues>({
    resolver: zodResolver(eventSchema),
  })

  const imageUrl = watch('imageUrl') ?? ''
  const [uploading, setUploading] = useState(false)

  const handleImageUpload = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0]
    if (!file) return
    if (!['image/jpeg', 'image/png', 'image/webp'].includes(file.type)) {
      toast.error('Formato no permitido. Usa JPEG, PNG o WEBP.')
      return
    }
    if (file.size > 5 * 1024 * 1024) {
      toast.error('La imagen supera el límite de 5 MB.')
      return
    }
    setUploading(true)
    try {
      const url = await eventRepository.uploadImage(file)
      setValue('imageUrl', url, { shouldValidate: true })
    } catch {
      toast.error('Error al subir la imagen. Intenta de nuevo.')
    } finally {
      setUploading(false)
    }
  }

  const { mutateAsync: createEvent, isPending } = useMutation({
    mutationFn: (req: Parameters<CreateEventUseCase['execute']>[0]) =>
      new CreateEventUseCase(eventRepository).execute(req),
  })

  const submittingRef = useRef(false)

  const onSubmit = async (values: EventFormValues) => {
    if (submittingRef.current) return
    submittingRef.current = true
    try {
      const event = await createEvent({
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

      if (ticketTypes.length > 0) {
        const useCase = new CreateTicketTypeUseCase(ticketTypeRepository)
        await Promise.all(
          ticketTypes.map((tt) => {
            const req: CreateTicketTypeRequest = {
              name:          tt.name,
              description:   tt.description || undefined,
              price:         tt.price,
              currency:      'USD',
              totalQuantity: tt.totalQuantity,
              saleStartDate: combineDT(tt.saleStartDate, tt.saleStartTime, '00:00'),
              saleEndDate:   combineDT(tt.saleEndDate,   tt.saleEndTime,   '23:59'),
            }
            return useCase.execute(event.id, req)
          }),
        )
      }

      queryClient.invalidateQueries({ queryKey: ['my-events'] })
      queryClient.invalidateQueries({ queryKey: ['events'] })
      toast.success('Evento creado con éxito')
      navigate('/my-events')
    } catch {
      toast.error('Error al crear el evento. Intenta de nuevo.')
    } finally {
      submittingRef.current = false
    }
  }

  const handleAddTicket = (values: TicketFormValues) => {
    setTicketTypes((prev) => [...prev, values])
    setShowTicketForm(false)
  }

  const handleRemoveTicket = (index: number) => {
    setTicketTypes((prev) => prev.filter((_, i) => i !== index))
  }

  return (
    <div style={styles.container}>
      <div style={styles.header}>
        <button style={styles.back} onClick={() => navigate('/my-events')}>← Mis eventos</button>
        <h1 style={styles.heading}>Crear evento</h1>
      </div>

      <form onSubmit={handleSubmit(onSubmit)} style={styles.form}>

        {/* ── Datos del evento ── */}
        <section style={styles.section}>
          <h2 style={styles.sectionTitle}>Datos del evento</h2>

          <div style={styles.fields}>
            <div style={styles.field}>
              <label className="ef-label">Título *</label>
              <input type="text" {...register('title')} className="ef-input" />
              {errors.title && <span className="ef-error">{errors.title.message}</span>}
            </div>

            <div style={styles.field}>
              <label className="ef-label">Descripción</label>
              <textarea {...register('description')} className="ef-input" rows={3} style={{ resize: 'vertical' }} />
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
              <input type="text" {...register('venue')} className="ef-input" placeholder="Nombre del lugar" />
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
                <input type="text" {...register('country')} className="ef-input" list="country-list" />
                <datalist id="country-list">
                  {COUNTRIES.map((c) => <option key={c} value={c} />)}
                </datalist>
                {errors.country && <span className="ef-error">{errors.country.message}</span>}
              </div>
            </div>

            <div style={styles.row}>
              <div style={styles.field}>
                <label className="ef-label">Fecha de inicio *</label>
                <input type="date" min={todayStr()} {...register('startDate')} className="ef-input" />
                {errors.startDate && <span className="ef-error">{errors.startDate.message}</span>}
              </div>
              <div style={styles.field}>
                <label className="ef-label">Fecha de fin</label>
                <input type="date" min={todayStr()} {...register('endDate')} className="ef-input" />
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
          </div>
        </section>

        {/* ── Tipos de ticket ── */}
        <section style={styles.ticketSection}>
          <div style={styles.sectionHeader}>
            <div>
              <h2 style={styles.sectionTitle}>Tipos de ticket</h2>
              <p style={styles.sectionSub}>Podés agregar más tipos después de crear el evento.</p>
            </div>
            {!showTicketForm && (
              <button type="button" className="ef-btn-ghost" style={styles.addBtn} onClick={() => setShowTicketForm(true)}>
                + Agregar tipo
              </button>
            )}
          </div>

          {showTicketForm && (
            <div style={styles.ticketFormBox}>
              <TicketTypeForm onAdd={handleAddTicket} onCancel={() => setShowTicketForm(false)} />
            </div>
          )}

          {ticketTypes.length === 0 && !showTicketForm ? (
            <p style={styles.emptyTickets}>Sin tipos de ticket. Los compradores no podrán adquirir entradas hasta que agregues al menos uno.</p>
          ) : (
            <div style={styles.ticketList}>
              {ticketTypes.map((tt, i) => (
                <div key={i} style={styles.ticketCard}>
                  <div style={styles.ticketInfo}>
                    <span style={styles.ticketName}>{tt.name}</span>
                    <span style={styles.ticketDetail}>
                      USD {tt.price.toFixed(2)} · {tt.totalQuantity} unidades
                    </span>
                    {tt.description && <span style={styles.ticketDesc}>{tt.description}</span>}
                    {(tt.saleStartDate || tt.saleEndDate) && (
                      <span style={styles.ticketSaleDates}>
                        Venta:{' '}
                        {tt.saleStartDate
                          ? `desde ${formatDateTimeShort(tt.saleStartDate)}`
                          : 'ya abierta'
                        }
                        {tt.saleEndDate
                          ? ` · hasta ${formatDateTimeShort(tt.saleEndDate)}`
                          : ''
                        }
                      </span>
                    )}
                  </div>
                  <button
                    type="button"
                    className="ef-btn-danger"
                    style={styles.removeBtn}
                    onClick={() => handleRemoveTicket(i)}
                  >
                    Quitar
                  </button>
                </div>
              ))}
            </div>
          )}
        </section>

        {/* ── Acciones ── */}
        <div style={styles.formActions}>
          <button type="button" className="ef-btn-ghost" onClick={() => navigate('/my-events')}>
            Cancelar
          </button>
          <button type="submit" className="ef-btn" disabled={isPending}>
            {isPending ? 'Creando...' : 'Crear evento'}
          </button>
        </div>
      </form>
    </div>
  )
}

// ── Styles ───────────────────────────────────────────────────────────────────

const styles: Record<string, React.CSSProperties> = {
  container:    { maxWidth: '720px', margin: '0 auto' },
  header:       { marginBottom: '1.5rem' },
  back:         { background: 'none', border: 'none', cursor: 'pointer', color: t.accent, fontSize: '0.9rem', padding: 0, marginBottom: '0.75rem', display: 'block', fontWeight: 500 },
  heading:      { fontSize: '1.75rem', fontWeight: 700, color: t.text },
  form:         { display: 'flex', flexDirection: 'column', gap: '1.5rem' },
  section:      { background: t.surface, border: `1px solid ${t.border}`, borderRadius: '10px', padding: '1.5rem' },
  ticketSection:{ borderTop: `1px solid ${t.border}`, paddingTop: '1.75rem' },
  sectionHeader:{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1rem' },
  sectionTitle: { fontSize: '1.15rem', fontWeight: 600, color: t.text },
  sectionSub:   { fontSize: '0.8rem', color: t.textDim, margin: 0 },
  fields:       { display: 'flex', flexDirection: 'column', gap: '1.125rem' },
  row:          { display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' },
  field:        { display: 'flex', flexDirection: 'column', gap: '0.35rem' },
  addBtn:       { padding: '0.45rem 1rem', fontSize: '0.875rem', whiteSpace: 'nowrap', flexShrink: 0 },
  ticketFormBox:{ background: t.surface, border: `1px solid ${t.border}`, borderRadius: '8px', padding: '1.25rem', marginBottom: '1rem' },
  ticketList:   { display: 'flex', flexDirection: 'column', gap: '0.625rem' },
  ticketCard:   { background: t.surface, border: `1px solid ${t.border}`, borderRadius: '8px', padding: '1rem 1.25rem', display: 'flex', justifyContent: 'space-between', alignItems: 'center', gap: '1rem' },
  ticketInfo:   { display: 'flex', flexDirection: 'column', gap: '0.2rem' },
  ticketName:   { fontWeight: 600, color: t.text, fontSize: '0.95rem' },
  ticketDetail: { color: t.accent, fontSize: '0.85rem', fontWeight: 500 },
  ticketDesc:     { color: t.textMuted, fontSize: '0.8rem' },
  ticketSaleDates:{ color: t.textDim, fontSize: '0.78rem', fontStyle: 'italic' },
  removeBtn:    { padding: '0.3rem 0.7rem', fontSize: '0.78rem', flexShrink: 0 },
  emptyTickets: { color: t.textDim, fontSize: '0.85rem', lineHeight: 1.5, marginTop: '0.5rem' },
  formActions:  { display: 'flex', justifyContent: 'flex-end', gap: '0.75rem' },
  uploadHint:   { fontSize: '0.82rem', color: t.textDim },
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
