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
        <label htmlFor="email" className="ef-label">Email</label>
        <input id="email" type="email" {...register('email')} className="ef-input" placeholder="tu@email.com" />
        {errors.email && <span className="ef-error">{errors.email.message}</span>}
      </div>

      <div style={styles.field}>
        <label htmlFor="password" className="ef-label">Contraseña</label>
        <input id="password" type="password" {...register('password')} className="ef-input" placeholder="••••••••" />
        {errors.password && <span className="ef-error">{errors.password.message}</span>}
      </div>

      <button type="submit" disabled={isLoading} className="ef-btn ef-btn-full" style={{ marginTop: '0.5rem' }}>
        {isLoading ? 'Ingresando...' : 'Ingresar'}
      </button>
    </form>
  )
}

const styles: Record<string, React.CSSProperties> = {
  form:  { display: 'flex', flexDirection: 'column', gap: '1rem' },
  field: { display: 'flex', flexDirection: 'column' },
}
