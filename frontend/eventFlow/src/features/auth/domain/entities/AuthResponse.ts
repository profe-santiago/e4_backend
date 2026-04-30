export interface AuthResponse {
  userId: string
  email: string
  token: string
  role: 'BUYER' | 'ADMIN'
}

export const decodeJwtPayload = (token: string): { role: 'BUYER' | 'ADMIN'; userId: string } => {
  const base64 = token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/')
  const payload = JSON.parse(window.atob(base64)) as { role: string; sub: string }
  return {
    role: payload.role === 'ADMIN' ? 'ADMIN' : 'BUYER',
    userId: payload.sub,
  }
}
