import { useEffect, useState } from 'react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { useProfile } from '../hooks/useProfile'
import { useUpdateProfile } from '../hooks/useUpdateProfile'
import { useDeleteAccount } from '../hooks/useDeleteAccount'

const schema = z.object({
  firstName: z.string().min(1, 'Requerido'),
  lastName:  z.string().min(1, 'Requerido'),
  phone:     z.string().optional(),
  birthDate: z.string().optional(),
  avatarUrl: z.string().url('URL inválida').optional().or(z.literal('')),
})

type FormValues = z.infer<typeof schema>

export const ProfilePage = () => {
  const { data: user, isLoading, isError } = useProfile()
  const { mutate: update, isPending: isUpdating } = useUpdateProfile()
  const { mutate: deleteAccount, isPending: isDeleting } = useDeleteAccount()
  const [showDeleteConfirm, setShowDeleteConfirm] = useState(false)

  const { register, handleSubmit, reset, formState: { errors, isDirty } } = useForm<FormValues>({
    resolver: zodResolver(schema),
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

  if (isLoading) return <div style={styles.feedback}>Cargando perfil...</div>
  if (isError || !user) return <div style={styles.error}>Error al cargar el perfil.</div>

  return (
    <div style={styles.container}>
      <h1 style={styles.heading}>Mi perfil</h1>
      <p style={styles.email}>{user.email}</p>

      <form onSubmit={handleSubmit(onSubmit)} style={styles.form}>
        <div style={styles.row}>
          <div style={styles.field}>
            <label htmlFor="firstName">Nombre</label>
            <input id="firstName" type="text" {...register('firstName')} style={styles.input} />
            {errors.firstName && <span style={styles.error}>{errors.firstName.message}</span>}
          </div>
          <div style={styles.field}>
            <label htmlFor="lastName">Apellido</label>
            <input id="lastName" type="text" {...register('lastName')} style={styles.input} />
            {errors.lastName && <span style={styles.error}>{errors.lastName.message}</span>}
          </div>
        </div>

        <div style={styles.field}>
          <label htmlFor="phone">Teléfono</label>
          <input id="phone" type="tel" {...register('phone')} style={styles.input} placeholder="+54 9 11 0000-0000" />
        </div>

        <div style={styles.field}>
          <label htmlFor="birthDate">Fecha de nacimiento</label>
          <input id="birthDate" type="date" {...register('birthDate')} style={styles.input} />
        </div>

        <div style={styles.field}>
          <label htmlFor="avatarUrl">URL de avatar</label>
          <input id="avatarUrl" type="url" {...register('avatarUrl')} style={styles.input} placeholder="https://..." />
          {errors.avatarUrl && <span style={styles.errorMsg}>{errors.avatarUrl.message}</span>}
        </div>

        <button
          type="submit"
          disabled={isUpdating || !isDirty}
          style={{ ...styles.saveBtn, ...(!isDirty ? styles.disabled : {}) }}
        >
          {isUpdating ? 'Guardando...' : 'Guardar cambios'}
        </button>
      </form>

      <div style={styles.dangerZone}>
        <h2 style={styles.dangerTitle}>Zona de peligro</h2>

        {!showDeleteConfirm
          ? (
            <button style={styles.deleteBtn} onClick={() => setShowDeleteConfirm(true)}>
              Eliminar cuenta
            </button>
          )
          : (
            <div style={styles.confirmBox}>
              <p style={styles.confirmText}>
                ¿Estás seguro? Esta acción <strong>no se puede deshacer</strong>.
              </p>
              <div style={styles.confirmActions}>
                <button style={styles.cancelBtn} onClick={() => setShowDeleteConfirm(false)}>
                  Cancelar
                </button>
                <button
                  style={styles.confirmDeleteBtn}
                  disabled={isDeleting}
                  onClick={() => deleteAccount()}
                >
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
  container: { maxWidth: '640px', margin: '0 auto', padding: '2rem 1rem' },
  heading: { fontSize: '1.75rem', fontWeight: 700, marginBottom: '0.25rem' },
  email: { color: '#718096', marginBottom: '2rem' },
  form: { display: 'flex', flexDirection: 'column', gap: '1.25rem', marginBottom: '3rem' },
  row: { display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' },
  field: { display: 'flex', flexDirection: 'column', gap: '0.25rem' },
  input: { padding: '0.5rem', fontSize: '1rem', borderRadius: '4px', border: '1px solid #cbd5e0' },
  error: { color: '#e53e3e', fontSize: '0.85rem' },
  errorMsg: { color: '#e53e3e', fontSize: '0.85rem' },
  saveBtn: { padding: '0.75rem', fontSize: '1rem', background: '#3182ce', color: '#fff', border: 'none', borderRadius: '4px', cursor: 'pointer', fontWeight: 600 },
  disabled: { background: '#a0aec0', cursor: 'not-allowed' },
  dangerZone: { borderTop: '1px solid #fed7d7', paddingTop: '1.5rem' },
  dangerTitle: { fontSize: '1rem', fontWeight: 600, color: '#c53030', marginBottom: '1rem' },
  deleteBtn: { padding: '0.6rem 1.25rem', border: '1px solid #e53e3e', borderRadius: '4px', color: '#e53e3e', background: 'none', cursor: 'pointer' },
  confirmBox: { background: '#fff5f5', border: '1px solid #fed7d7', borderRadius: '8px', padding: '1.25rem', display: 'flex', flexDirection: 'column', gap: '1rem' },
  confirmText: { margin: 0, color: '#333' },
  confirmActions: { display: 'flex', gap: '0.75rem' },
  cancelBtn: { padding: '0.6rem 1.25rem', border: '1px solid #cbd5e0', borderRadius: '4px', background: '#fff', cursor: 'pointer' },
  confirmDeleteBtn: { padding: '0.6rem 1.25rem', background: '#e53e3e', color: '#fff', border: 'none', borderRadius: '4px', cursor: 'pointer', fontWeight: 600 },
  feedback: { textAlign: 'center', padding: '4rem', color: '#555' },
}
