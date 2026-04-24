import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { useLogin } from '../hooks/useLogin'

const schema = z.object({
  email: z.string().email('Email inválido'),
  password: z.string().min(6, 'Mínimo 6 caracteres'),
})

type FormValues = z.infer<typeof schema>

export const LoginForm = () => {
  const { login, isLoading } = useLogin()
  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<FormValues>({ resolver: zodResolver(schema) })

  const onSubmit = (values: FormValues) => login(values.email, values.password)

  return (
    <form onSubmit={handleSubmit(onSubmit)} style={styles.form}>
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

      <button type="submit" disabled={isLoading} style={styles.button}>
        {isLoading ? 'Ingresando...' : 'Ingresar'}
      </button>
    </form>
  )
}

const styles: Record<string, React.CSSProperties> = {
  form: { display: 'flex', flexDirection: 'column', gap: '1rem' },
  field: { display: 'flex', flexDirection: 'column', gap: '0.25rem' },
  input: { padding: '0.5rem', fontSize: '1rem', borderRadius: '4px', border: '1px solid #ccc' },
  error: { color: '#e53e3e', fontSize: '0.85rem' },
  button: { padding: '0.75rem', fontSize: '1rem', cursor: 'pointer', borderRadius: '4px', border: 'none', background: '#3182ce', color: '#fff' },
}
