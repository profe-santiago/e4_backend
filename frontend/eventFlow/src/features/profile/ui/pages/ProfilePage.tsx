import { useEffect, useState } from 'react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import axios from 'axios'
import { useProfile } from '../hooks/useProfile'
import { useUpdateProfile } from '../hooks/useUpdateProfile'
import { useDeleteAccount } from '../hooks/useDeleteAccount'
import { useCreateProfile } from '../hooks/useCreateProfile'
import { useAuthStore } from '@/store/auth.store'
import { t } from '@/shared/config/theme'
import { formatDate } from '@/shared/utils/formatDate'

const schema = z.object({
  firstName: z.string().min(1, 'Requerido'),
  lastName:  z.string().min(1, 'Requerido'),
  phone:     z.string().optional(),
  birthDate: z.string().optional(),
  avatarUrl: z.string().url('URL inválida').optional().or(z.literal('')),
})
type FormValues = z.infer<typeof schema>

const createSchema = z.object({
  firstName: z.string().min(1, 'Requerido'),
  lastName:  z.string().min(1, 'Requerido'),
})
type CreateFormValues = z.infer<typeof createSchema>

const getInitials = (firstName: string, lastName: string) =>
  `${firstName.charAt(0)}${lastName.charAt(0)}`.toUpperCase()

const Avatar = ({ url, initials, size = 80 }: { url?: string | null; initials: string; size?: number }) => (
  <div style={{
    width: size, height: size, borderRadius: '50%',
    background: url ? 'transparent' : `linear-gradient(135deg, ${t.accent}, #088B87)`,
    display: 'flex', alignItems: 'center', justifyContent: 'center',
    fontSize: size * 0.35, fontWeight: 700, color: '#fff',
    flexShrink: 0, overflow: 'hidden',
    border: `3px solid ${t.border2}`,
    boxShadow: `0 0 0 4px ${t.surface}`,
  }}>
    {url
      ? <img src={url} alt="avatar" style={{ width: '100%', height: '100%', objectFit: 'cover' }} onError={(e) => { (e.target as HTMLImageElement).style.display = 'none' }} />
      : initials
    }
  </div>
)

export const ProfilePage = () => {
  const { data: user, isLoading, isError, error } = useProfile()
  const { mutate: update, isPending: isUpdating } = useUpdateProfile()
  const { mutate: deleteAccount, isPending: isDeleting } = useDeleteAccount()
  const { mutate: createProfile, isPending: isCreating } = useCreateProfile()
  const authEmail = useAuthStore((s) => s.user?.email ?? '')
  const [showDeleteConfirm, setShowDeleteConfirm] = useState(false)

  const { register, handleSubmit, reset, watch, formState: { errors, isDirty } } = useForm<FormValues>({
    resolver: zodResolver(schema),
  })

  const { register: registerCreate, handleSubmit: handleCreateSubmit, formState: { errors: createErrors } } = useForm<CreateFormValues>({
    resolver: zodResolver(createSchema),
  })

  useEffect(() => {
    if (user) {
      reset({
        firstName: user.firstName,
        lastName:  user.lastName,
        phone:     user.phone ?? '',
        birthDate: user.birthDate ?? '',
        avatarUrl: user.avatarUrl ?? '',
      })
    }
  }, [user, reset])

  const watchedAvatar    = watch('avatarUrl')
  const watchedFirstName = watch('firstName')
  const watchedLastName  = watch('lastName')

  const onSubmit = (values: FormValues) => {
    update({
      firstName: values.firstName,
      lastName:  values.lastName,
      phone:     values.phone || undefined,
      birthDate: values.birthDate || undefined,
      avatarUrl: values.avatarUrl || undefined,
    })
  }

  const is404 = isError && axios.isAxiosError(error) && error.response?.status === 404

  if (isLoading) return <div style={styles.feedback}>Cargando perfil...</div>

  if (is404) return (
    <div style={styles.container}>
      <div style={styles.card}>
        <h1 style={styles.cardTitle}>Completa tu perfil</h1>
        <p style={{ color: t.textMuted, marginBottom: '1.5rem', fontSize: '0.9rem', lineHeight: 1.6 }}>
          Tu cuenta fue creada pero aún no tiene un perfil. Completá los datos para continuar.
        </p>
        <form onSubmit={handleCreateSubmit((v) => createProfile({ ...v, email: authEmail }))} style={styles.form}>
          <div style={styles.field}>
            <label className="ef-label">Correo electrónico</label>
            <input type="email" value={authEmail} readOnly className="ef-input" style={{ opacity: 0.5 }} />
          </div>
          <div style={styles.row}>
            <div style={styles.field}>
              <label className="ef-label">Nombre</label>
              <input type="text" {...registerCreate('firstName')} className="ef-input" />
              {createErrors.firstName && <span className="ef-error">{createErrors.firstName.message}</span>}
            </div>
            <div style={styles.field}>
              <label className="ef-label">Apellido</label>
              <input type="text" {...registerCreate('lastName')} className="ef-input" />
              {createErrors.lastName && <span className="ef-error">{createErrors.lastName.message}</span>}
            </div>
          </div>
          <button type="submit" disabled={isCreating} className="ef-btn" style={{ alignSelf: 'flex-start' }}>
            {isCreating ? 'Guardando...' : 'Guardar perfil'}
          </button>
        </form>
      </div>
    </div>
  )

  if (isError || !user) return <div style={styles.error}>Error al cargar el perfil.</div>

  const previewInitials = getInitials(
    watchedFirstName || user.firstName,
    watchedLastName  || user.lastName,
  )

  return (
    <div style={styles.container}>

      {/* ── Hero ── */}
      <div style={styles.hero}>
        <Avatar url={watchedAvatar || user.avatarUrl} initials={previewInitials} size={88} />
        <div style={styles.heroInfo}>
          <h1 style={styles.heroName}>{user.firstName} {user.lastName}</h1>
          <span style={styles.heroBadge}>{user.email}</span>
          <div style={styles.heroMeta}>
            <span style={styles.heroMetaItem}>
              Miembro desde {formatDate(user.createdAt)}
            </span>
            {user.birthDate && (
              <span style={styles.heroMetaItem}>
                · Nacido el {formatDate(user.birthDate + 'T12:00')}
              </span>
            )}
          </div>
        </div>
      </div>

      {/* ── Formulario ── */}
      <div style={styles.card}>
        <h2 style={styles.cardTitle}>Información personal</h2>
        <form onSubmit={handleSubmit(onSubmit)} style={styles.form}>
          <div style={styles.row}>
            <div style={styles.field}>
              <label className="ef-label">Nombre</label>
              <input type="text" {...register('firstName')} className="ef-input" />
              {errors.firstName && <span className="ef-error">{errors.firstName.message}</span>}
            </div>
            <div style={styles.field}>
              <label className="ef-label">Apellido</label>
              <input type="text" {...register('lastName')} className="ef-input" />
              {errors.lastName && <span className="ef-error">{errors.lastName.message}</span>}
            </div>
          </div>

          <div style={styles.row}>
            <div style={styles.field}>
              <label className="ef-label">Teléfono</label>
              <input type="tel" {...register('phone')} className="ef-input" placeholder="+52 55 0000 0000" />
            </div>
            <div style={styles.field}>
              <label className="ef-label">Fecha de nacimiento</label>
              <input type="date" {...register('birthDate')} className="ef-input" />
            </div>
          </div>

          <div style={styles.field}>
            <label className="ef-label">URL de avatar</label>
            <div style={styles.avatarRow}>
              <Avatar url={watchedAvatar} initials={previewInitials} size={40} />
              <input type="url" {...register('avatarUrl')} className="ef-input" placeholder="https://..." style={{ flex: 1 }} />
            </div>
            {errors.avatarUrl && <span className="ef-error">{errors.avatarUrl.message}</span>}
            <span style={styles.hint}>Vista previa en tiempo real</span>
          </div>

          <div style={styles.formFooter}>
            <span style={styles.lastUpdated}>
              Última actualización: {formatDate(user.updatedAt)}
            </span>
            <button
              type="submit"
              disabled={isUpdating || !isDirty}
              className="ef-btn"
              style={{ opacity: !isDirty ? 0.5 : 1 }}
            >
              {isUpdating ? 'Guardando...' : 'Guardar cambios'}
            </button>
          </div>
        </form>
      </div>

      {/* ── Zona de peligro ── */}
      <div style={styles.dangerCard}>
        <div style={styles.dangerHeader}>
          <h2 style={styles.dangerTitle}>Zona de peligro</h2>
          <p style={styles.dangerDesc}>Una vez que elimines tu cuenta no hay vuelta atrás.</p>
        </div>
        {!showDeleteConfirm
          ? (
            <button
              className="ef-btn-danger"
              style={{ padding: '0.5rem 1.25rem', fontSize: '0.875rem' }}
              onClick={() => setShowDeleteConfirm(true)}
            >
              Eliminar cuenta
            </button>
          )
          : (
            <div style={styles.confirmBox}>
              <p style={styles.confirmText}>
                ¿Estás seguro? Esta acción <strong>no se puede deshacer</strong> y perderás todos tus datos, tickets y órdenes.
              </p>
              <div style={styles.confirmActions}>
                <button className="ef-btn-ghost" onClick={() => setShowDeleteConfirm(false)}>
                  Cancelar
                </button>
                <button className="ef-btn-danger" disabled={isDeleting} onClick={() => deleteAccount()}>
                  {isDeleting ? 'Eliminando...' : 'Sí, eliminar mi cuenta'}
                </button>
              </div>
            </div>
          )
        }
      </div>
    </div>
  )
}

const styles: Record<string, React.CSSProperties> = {
  container:     { maxWidth: '680px', margin: '0 auto', display: 'flex', flexDirection: 'column', gap: '1.25rem' },
  feedback:      { textAlign: 'center', padding: '4rem', color: t.textMuted },
  error:         { textAlign: 'center', padding: '4rem', color: t.error },

  hero:          { display: 'flex', alignItems: 'center', gap: '1.5rem', padding: '1.75rem', background: t.surface, border: `1px solid ${t.border}`, borderRadius: '12px' },
  heroInfo:      { display: 'flex', flexDirection: 'column', gap: '0.35rem', minWidth: 0 },
  heroName:      { fontSize: '1.4rem', fontWeight: 700, color: t.text, margin: 0 },
  heroBadge:     { display: 'inline-block', fontSize: '0.8rem', color: t.accent, background: `${t.accent}18`, border: `1px solid ${t.accent}40`, borderRadius: '999px', padding: '0.15rem 0.65rem', alignSelf: 'flex-start' },
  heroMeta:      { display: 'flex', flexWrap: 'wrap' as const, gap: '0.25rem', marginTop: '0.1rem' },
  heroMetaItem:  { fontSize: '0.78rem', color: t.textDim },

  card:          { background: t.surface, border: `1px solid ${t.border}`, borderRadius: '12px', padding: '1.5rem' },
  cardTitle:     { fontSize: '1rem', fontWeight: 600, color: t.text, marginBottom: '1.25rem', marginTop: 0 },
  form:          { display: 'flex', flexDirection: 'column', gap: '1.1rem' },
  row:           { display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' },
  field:         { display: 'flex', flexDirection: 'column', gap: '0.35rem' },
  avatarRow:     { display: 'flex', alignItems: 'center', gap: '0.75rem' },
  hint:          { fontSize: '0.72rem', color: t.textDim, marginTop: '0.1rem' },
  formFooter:    { display: 'flex', justifyContent: 'space-between', alignItems: 'center', paddingTop: '0.5rem' },
  lastUpdated:   { fontSize: '0.75rem', color: t.textDim },

  dangerCard:    { background: `rgba(248,113,113,0.04)`, border: `1px solid rgba(248,113,113,0.2)`, borderRadius: '12px', padding: '1.5rem', display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', gap: '1rem', flexWrap: 'wrap' as const },
  dangerHeader:  { display: 'flex', flexDirection: 'column', gap: '0.25rem' },
  dangerTitle:   { fontSize: '1rem', fontWeight: 600, color: t.error, margin: 0 },
  dangerDesc:    { fontSize: '0.82rem', color: t.textMuted, margin: 0 },
  confirmBox:    { width: '100%', background: `rgba(248,113,113,0.06)`, border: `1px solid rgba(248,113,113,0.2)`, borderRadius: '8px', padding: '1.25rem', display: 'flex', flexDirection: 'column', gap: '1rem' },
  confirmText:   { margin: 0, color: t.textMuted, lineHeight: 1.6, fontSize: '0.9rem' },
  confirmActions:{ display: 'flex', gap: '0.75rem' },
}
