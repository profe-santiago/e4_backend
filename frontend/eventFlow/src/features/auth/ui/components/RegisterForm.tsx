import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { useRegister } from '../hooks/useRegister'

const schema = z
  .object({
    firstName: z.string().min(1, 'Requerido'),
    lastName: z.string().min(1, 'Requerido'),
    email: z.string().email('Email inválido'),
    password: z.string().min(6, 'Mínimo 6 caracteres'),
    confirmPassword: z.string(),
  })
  .refine((data) => data.password === data.confirmPassword, {
    message: 'Las contraseñas no coinciden',
    path: ['confirmPassword'],
  })

type FormValues = z.infer<typeof schema>

export const RegisterForm = () => {
  const { register: doRegister, isLoading } = useRegister()
  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<FormValues>({ resolver: zodResolver(schema) })

  const onSubmit = ({ firstName, lastName, email, password }: FormValues) =>
    doRegister({ firstName, lastName, email, password })

  return (
    <form onSubmit={handleSubmit(onSubmit)} style={styles.form}>
      <div style={styles.row}>
        <div style={styles.field}>
          <label htmlFor="firstName" className="ef-label">Nombre</label>
          <input id="firstName" type="text" {...register('firstName')} className="ef-input" placeholder="Juan" />
          {errors.firstName && <span className="ef-error">{errors.firstName.message}</span>}
        </div>
        <div style={styles.field}>
          <label htmlFor="lastName" className="ef-label">Apellido</label>
          <input id="lastName" type="text" {...register('lastName')} className="ef-input" placeholder="Pérez" />
          {errors.lastName && <span className="ef-error">{errors.lastName.message}</span>}
        </div>
      </div>

      <div style={styles.field}>
        <label htmlFor="email" className="ef-label">Email</label>
        <input id="email" type="email" {...register('email')} className="ef-input" placeholder="tu@email.com" />
        {errors.email && <span className="ef-error">{errors.email.message}</span>}
      </div>

      <div style={styles.field}>
        <label htmlFor="password" className="ef-label">Contraseña</label>
        <input id="password" type="password" {...register('password')} className="ef-input" placeholder="••••••••" />
        {errors.password && <span className="ef-error">{errors.password.message}</span>}
      </div>

      <div style={styles.field}>
        <label htmlFor="confirmPassword" className="ef-label">Confirmar contraseña</label>
        <input id="confirmPassword" type="password" {...register('confirmPassword')} className="ef-input" placeholder="••••••••" />
        {errors.confirmPassword && <span className="ef-error">{errors.confirmPassword.message}</span>}
      </div>

      <button type="submit" disabled={isLoading} className="ef-btn ef-btn-full" style={{ marginTop: '0.5rem' }}>
        {isLoading ? 'Registrando...' : 'Crear cuenta'}
      </button>
    </form>
  )
}

const styles: Record<string, React.CSSProperties> = {
  form:  { display: 'flex', flexDirection: 'column', gap: '1rem' },
  row:   { display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' },
  field: { display: 'flex', flexDirection: 'column' },
}
