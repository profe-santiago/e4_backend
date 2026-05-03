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

export const ProfilePage = () => {
  const { data: user, isLoading, isError, error } = useProfile()
  const { mutate: update, isPending: isUpdating } = useUpdateProfile()
  const { mutate: deleteAccount, isPending: isDeleting } = useDeleteAccount()
  const { mutate: createProfile, isPending: isCreating } = useCreateProfile()
  const authEmail = useAuthStore((s) => s.user?.email ?? '')
  const [showDeleteConfirm, setShowDeleteConfirm] = useState(false)

  const { register, handleSubmit, reset, formState: { errors, isDirty } } = useForm<FormValues>({
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
      <h1 style={styles.heading}>Completa tu perfil</h1>
      <p style={styles.subtext}>
        Tu cuenta fue creada pero aún no tiene un perfil. Completa los datos para continuar.
      </p>
      <form onSubmit={handleCreateSubmit((v) => createProfile({ ...v, email: authEmail }))} style={styles.form}>
        <div style={styles.field}>
          <label className="ef-label">Correo electrónico</label>
          <input type="email" value={authEmail} readOnly className="ef-input" style={{ opacity: 0.6 }} />
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
  )

  if (isError || !user) return <div style={styles.error}>Error al cargar el perfil.</div>

  return (
    <div style={styles.container}>
      <h1 style={styles.heading}>Mi perfil</h1>
      <p style={styles.email}>{user.email}</p>

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

        <div style={styles.field}>
          <label className="ef-label">Teléfono</label>
          <input type="tel" {...register('phone')} className="ef-input" placeholder="+54 9 11 0000-0000" />
        </div>

        <div style={styles.field}>
          <label className="ef-label">Fecha de nacimiento</label>
          <input type="date" {...register('birthDate')} className="ef-input" />
        </div>

        <div style={styles.field}>
          <label className="ef-label">URL de avatar</label>
          <input type="url" {...register('avatarUrl')} className="ef-input" placeholder="https://..." />
          {errors.avatarUrl && <span className="ef-error">{errors.avatarUrl.message}</span>}
        </div>

        <button
          type="submit"
          disabled={isUpdating || !isDirty}
          className="ef-btn"
          style={{ alignSelf: 'flex-start', opacity: !isDirty ? 0.5 : 1 }}
        >
          {isUpdating ? 'Guardando...' : 'Guardar cambios'}
        </button>
      </form>

      <div style={styles.dangerZone}>
        <h2 style={styles.dangerTitle}>Zona de peligro</h2>
        {!showDeleteConfirm
          ? (
            <button className="ef-btn-danger" style={{ padding: '0.5rem 1.25rem', fontSize: '0.875rem' }} onClick={() => setShowDeleteConfirm(true)}>
              Eliminar cuenta
            </button>
          )
          : (
            <div style={styles.confirmBox}>
              <p style={styles.confirmText}>
                ¿Estás seguro? Esta acción <strong>no se puede deshacer</strong>.
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
  container:      { maxWidth: '600px', margin: '0 auto' },
  heading:        { fontSize: '1.75rem', fontWeight: 700, color: t.text, marginBottom: '0.25rem' },
  subtext:        { color: t.textMuted, marginBottom: '2rem', fontSize: '0.9rem', lineHeight: 1.5 },
  email:          { color: t.textMuted, marginBottom: '2rem', fontSize: '0.9rem' },
  form:           { display: 'flex', flexDirection: 'column', gap: '1.25rem', marginBottom: '3rem' },
  row:            { display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' },
  field:          { display: 'flex', flexDirection: 'column', gap: '0.35rem' },
  dangerZone:     { borderTop: `1px solid rgba(248,113,113,0.2)`, paddingTop: '1.5rem' },
  dangerTitle:    { fontSize: '0.95rem', fontWeight: 600, color: t.error, marginBottom: '1rem' },
  confirmBox:     { background: 'rgba(248,113,113,0.06)', border: `1px solid rgba(248,113,113,0.2)`, borderRadius: '8px', padding: '1.25rem', display: 'flex', flexDirection: 'column', gap: '1rem' },
  confirmText:    { margin: 0, color: t.textMuted, lineHeight: 1.5 },
  confirmActions: { display: 'flex', gap: '0.75rem' },
  feedback:       { textAlign: 'center', padding: '4rem', color: t.textMuted },
  error:          { textAlign: 'center', padding: '4rem', color: t.error },
}
