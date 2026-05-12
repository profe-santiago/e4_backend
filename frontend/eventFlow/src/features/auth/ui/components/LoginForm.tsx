import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { useLogin } from '../hooks/useLogin'
import { t } from '@/shared/config/theme'

const EyeIcon = () => (
  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
    <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/>
    <circle cx="12" cy="12" r="3"/>
  </svg>
)

const EyeOffIcon = () => (
  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
    <path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"/>
    <line x1="1" y1="1" x2="23" y2="23"/>
  </svg>
)

const schema = z.object({
  email:    z.string().email('Email inválido'),
  password: z.string().min(1, 'Ingresá tu contraseña'),
})

type FormValues = z.infer<typeof schema>

export const LoginForm = () => {
  const { login, isLoading, credentialError } = useLogin()
  const [showPassword, setShowPassword] = useState(false)
  const { register, handleSubmit, formState: { errors } } = useForm<FormValues>({
    resolver: zodResolver(schema),
  })

  const onSubmit = (values: FormValues) => login(values.email, values.password)

  return (
    <form onSubmit={handleSubmit(onSubmit)} style={styles.form}>

      <div style={styles.field}>
        <label htmlFor="email" className="ef-label">Email</label>
        <input
          id="email"
          type="email"
          {...register('email')}
          className="ef-input"
          placeholder="tu@email.com"
          style={errors.email ? styles.inputError : undefined}
        />
        {errors.email && <span className="ef-error">{errors.email.message}</span>}
      </div>

      <div style={styles.field}>
        <label htmlFor="password" className="ef-label">Contraseña</label>
        <div style={styles.passwordWrapper}>
          <input
            id="password"
            type={showPassword ? 'text' : 'password'}
            {...register('password')}
            className="ef-input"
            placeholder="••••••••"
            style={{ ...(errors.password ? styles.inputError : {}), paddingRight: '3.5rem' }}
          />
          <button
            type="button"
            onClick={() => setShowPassword(v => !v)}
            style={styles.eyeBtn}
            tabIndex={-1}
          >
            {showPassword ? <EyeOffIcon /> : <EyeIcon />}
          </button>
        </div>
        {errors.password && <span className="ef-error">{errors.password.message}</span>}
      </div>

      {credentialError && (
        <div style={styles.serverError}>{credentialError}</div>
      )}

      <button
        type="submit"
        disabled={isLoading}
        className="ef-btn ef-btn-full"
        style={{ marginTop: '0.25rem' }}
      >
        {isLoading ? 'Ingresando...' : 'Ingresar'}
      </button>

    </form>
  )
}

const styles: Record<string, React.CSSProperties> = {
  form:            { display: 'flex', flexDirection: 'column', gap: '1rem' },
  field:           { display: 'flex', flexDirection: 'column', gap: '0.3rem' },
  passwordWrapper: { position: 'relative' },
  eyeBtn:          { position: 'absolute', right: '0.75rem', top: '50%', transform: 'translateY(-50%)', background: 'none', border: 'none', cursor: 'pointer', color: t.textMuted, padding: '0.25rem', lineHeight: 1, display: 'flex', alignItems: 'center' },
  inputError:      { borderColor: t.error },
  serverError:     { background: 'rgba(248,113,113,0.08)', border: `1px solid rgba(248,113,113,0.3)`, borderRadius: '8px', padding: '0.65rem 0.875rem', color: t.error, fontSize: '0.875rem', textAlign: 'center' },
}
