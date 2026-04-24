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
        <label htmlFor="email">Email</label>
        <input id="email" type="email" {...register('email')} style={styles.input} />
        {errors.email && <span style={styles.error}>{errors.email.message}</span>}
      </div>

      <div style={styles.field}>
        <label htmlFor="password">Contraseña</label>
        <input id="password" type="password" {...register('password')} style={styles.input} />
        {errors.password && <span style={styles.error}>{errors.password.message}</span>}
      </div>

      <div style={styles.field}>
        <label htmlFor="confirmPassword">Confirmar contraseña</label>
        <input id="confirmPassword" type="password" {...register('confirmPassword')} style={styles.input} />
        {errors.confirmPassword && <span style={styles.error}>{errors.confirmPassword.message}</span>}
      </div>

      <button type="submit" disabled={isLoading} style={styles.button}>
        {isLoading ? 'Registrando...' : 'Crear cuenta'}
      </button>
    </form>
  )
}

const styles: Record<string, React.CSSProperties> = {
  form: { display: 'flex', flexDirection: 'column', gap: '1rem' },
  row: { display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' },
  field: { display: 'flex', flexDirection: 'column', gap: '0.25rem' },
  input: { padding: '0.5rem', fontSize: '1rem', borderRadius: '4px', border: '1px solid #ccc' },
  error: { color: '#e53e3e', fontSize: '0.85rem' },
  button: { padding: '0.75rem', fontSize: '1rem', cursor: 'pointer', borderRadius: '4px', border: 'none', background: '#3182ce', color: '#fff' },
}
