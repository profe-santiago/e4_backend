export interface AuthResponse {
  userId: string
  email: string
  token: string
  role: 'BUYER' | 'ADMIN'
}

export const decodeJwtRole = (token: string): 'BUYER' | 'ADMIN' => {
  const base64 = token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/')
  const payload = JSON.parse(window.atob(base64)) as { role: string }
  return payload.role === 'ADMIN' ? 'ADMIN' : 'BUYER'
}
