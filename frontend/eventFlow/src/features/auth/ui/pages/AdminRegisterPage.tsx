import { useState } from 'react'
import { useForm, useWatch } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { Link } from 'react-router-dom'
import { useRegisterAdmin } from '../hooks/useRegisterAdmin'
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

const passwordRules = [
  { id: 'length',  label: 'Mínimo 8 caracteres',          test: (v: string) => v.length >= 8 },
  { id: 'upper',   label: 'Una letra mayúscula',           test: (v: string) => /[A-Z]/.test(v) },
  { id: 'lower',   label: 'Una letra minúscula',           test: (v: string) => /[a-z]/.test(v) },
  { id: 'number',  label: 'Un número',                     test: (v: string) => /[0-9]/.test(v) },
  { id: 'special', label: 'Un carácter especial (!@#$…)',  test: (v: string) => /[^A-Za-z0-9]/.test(v) },
]

const schema = z
  .object({
    email:           z.string().email('Email inválido'),
    password:        z.string()
      .min(8,                'Mínimo 8 caracteres')
      .regex(/[A-Z]/,        'Debe tener al menos una mayúscula')
      .regex(/[a-z]/,        'Debe tener al menos una minúscula')
      .regex(/[0-9]/,        'Debe tener al menos un número')
      .regex(/[^A-Za-z0-9]/, 'Debe tener al menos un carácter especial'),
    confirmPassword: z.string(),
  })
  .refine((data) => data.password === data.confirmPassword, {
    message: 'Las contraseñas no coinciden',
    path: ['confirmPassword'],
  })

type FormValues = z.infer<typeof schema>

const PasswordChecklist = ({ value, hasError }: { value: string; hasError: boolean }) => {
  if (!value && !hasError) return null
  return (
    <ul style={{ ...styles.checklist, ...(hasError ? styles.checklistError : {}) }}>
      {passwordRules.map(rule => {
        const ok = rule.test(value)
        const color = ok ? t.success : hasError ? t.error : t.textMuted
        return (
          <li key={rule.id} style={{ ...styles.checkItem, color }}>
            <span style={styles.checkIcon}>{ok ? '✓' : '✕'}</span>
            {rule.label}
          </li>
        )
      })}
    </ul>
  )
}

export default function AdminRegisterPage() {
  const { registerAdmin, isLoading } = useRegisterAdmin()
  const [showPassword, setShowPassword] = useState(false)
  const [showConfirm,  setShowConfirm]  = useState(false)

  const { register, handleSubmit, control, formState: { errors } } = useForm<FormValues>({
    resolver: zodResolver(schema),
  })

  const passwordValue = useWatch({ control, name: 'password', defaultValue: '' })

  const onSubmit = ({ email, password }: FormValues) =>
    registerAdmin({ email, password })

  return (
    <div style={styles.container}>
      <div style={styles.glow} />
      <Link to="/" style={styles.back}>← Volver a eventos</Link>

      <div style={styles.card}>
        <div style={styles.brand}>
          <div style={styles.brandIcon}>⬡</div>
          <span style={styles.brandName}>EventFlow</span>
        </div>

        <h1 style={styles.heading}>Registro de organizador</h1>
        <p style={styles.sub}>Crea tu cuenta para publicar y gestionar eventos</p>

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
              <button type="button" onClick={() => setShowPassword(v => !v)} style={styles.eyeBtn} tabIndex={-1}>
                {showPassword ? <EyeOffIcon /> : <EyeIcon />}
              </button>
            </div>
            <PasswordChecklist value={passwordValue} hasError={!!errors.password} />
          </div>

          <div style={styles.field}>
            <label htmlFor="confirmPassword" className="ef-label">Confirmar contraseña</label>
            <div style={styles.passwordWrapper}>
              <input
                id="confirmPassword"
                type={showConfirm ? 'text' : 'password'}
                {...register('confirmPassword')}
                className="ef-input"
                placeholder="••••••••"
                style={{ ...(errors.confirmPassword ? styles.inputError : {}), paddingRight: '3.5rem' }}
              />
              <button type="button" onClick={() => setShowConfirm(v => !v)} style={styles.eyeBtn} tabIndex={-1}>
                {showConfirm ? <EyeOffIcon /> : <EyeIcon />}
              </button>
            </div>
            {errors.confirmPassword && <span className="ef-error">{errors.confirmPassword.message}</span>}
          </div>

          <button
            type="submit"
            disabled={isLoading}
            className="ef-btn ef-btn-full"
            style={{ marginTop: '0.5rem' }}
          >
            {isLoading ? 'Creando cuenta...' : 'Crear cuenta de organizador'}
          </button>
        </form>

        <p style={styles.footer}>
          ¿Ya tienes cuenta?{' '}
          <Link to="/login" className="ef-link">Inicia sesión</Link>
        </p>
      </div>
    </div>
  )
}

const styles: Record<string, React.CSSProperties> = {
  container:      { minHeight: '100vh', display: 'flex', alignItems: 'center', justifyContent: 'center', background: t.base, padding: '1.5rem', position: 'relative', overflow: 'hidden' },
  glow:           { position: 'absolute', top: '-20%', left: '50%', transform: 'translateX(-50%)', width: '600px', height: '600px', background: 'radial-gradient(circle, rgba(10,173,168,0.12) 0%, transparent 70%)', pointerEvents: 'none' },
  card:           { position: 'relative', width: '100%', maxWidth: '480px', background: t.surface, border: `1px solid ${t.border}`, borderRadius: '14px', padding: '2.5rem 2rem', boxShadow: '0 24px 64px rgba(0,0,0,0.4)' },
  brand:          { display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '0.5rem', marginBottom: '2rem' },
  brandIcon:      { fontSize: '1.5rem', color: t.accent, lineHeight: 1 },
  brandName:      { fontSize: '1.35rem', fontWeight: 700, color: t.text, letterSpacing: '0.03em' },
  heading:        { textAlign: 'center', fontSize: '1.4rem', fontWeight: 700, color: t.text, marginBottom: '0.4rem' },
  sub:            { textAlign: 'center', fontSize: '0.9rem', color: t.textMuted, marginBottom: '1.75rem' },
  form:           { display: 'flex', flexDirection: 'column', gap: '1rem' },
  field:          { display: 'flex', flexDirection: 'column', gap: '0.3rem' },
  passwordWrapper:{ position: 'relative' },
  eyeBtn:         { position: 'absolute', right: '0.75rem', top: '50%', transform: 'translateY(-50%)', background: 'none', border: 'none', cursor: 'pointer', color: t.textMuted, padding: '0.25rem', lineHeight: 1, display: 'flex', alignItems: 'center' },
  inputError:     { borderColor: t.error },
  footer:         { textAlign: 'center', marginTop: '1.5rem', fontSize: '0.875rem', color: t.textMuted },
  back:           { position: 'absolute' as const, top: '1.25rem', left: '1.5rem', textDecoration: 'none', color: t.textMuted, fontSize: '0.875rem', fontWeight: 500, zIndex: 1 },
  checklist:      { listStyle: 'none', margin: '0.25rem 0 0', padding: '0.5rem 0.75rem', background: 'rgba(255,255,255,0.04)', borderRadius: '8px', display: 'flex', flexDirection: 'column', gap: '0.2rem' },
  checklistError: { background: 'rgba(248,113,113,0.06)', border: '1px solid rgba(248,113,113,0.2)' },
  checkItem:      { fontSize: '0.78rem', display: 'flex', alignItems: 'center', gap: '0.4rem', transition: 'color 0.2s' },
  checkIcon:      { fontWeight: 700, width: '0.9rem', textAlign: 'center' },
}
