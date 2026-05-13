import { create } from 'zustand'
import { TokenStorage } from '@/core/storage/TokenStorage'

const USER_KEY = 'auth_user'

interface AuthUser {
  userId: string
  email: string
  role: 'BUYER' | 'ADMIN'
}

interface AuthState {
  user: AuthUser | null
  token: string | null
  isAuthenticated: boolean
  isAdmin: boolean
  setAuth: (user: AuthUser, token: string) => void
  logout: () => void
}

const loadUser = (): AuthUser | null => {
  try {
    const stored = localStorage.getItem(USER_KEY)
    return stored ? (JSON.parse(stored) as AuthUser) : null
  } catch {
    return null
  }
}

const isTokenExpired = (token: string): boolean => {
  try {
    const payload = JSON.parse(atob(token.split('.')[1]))
    return payload.exp * 1000 < Date.now()
  } catch {
    return true
  }
}

const storedToken = TokenStorage.get()
const tokenValid = storedToken ? !isTokenExpired(storedToken) : false

if (!tokenValid && storedToken) {
  TokenStorage.remove()
  localStorage.removeItem(USER_KEY)
}

const storedUser = tokenValid ? loadUser() : null

export const useAuthStore = create<AuthState>((set) => ({
  user: storedUser,
  token: storedToken,
  isAuthenticated: tokenValid,
  isAdmin: storedUser?.role === 'ADMIN',

  setAuth: (user, token) => {
    TokenStorage.set(token)
    localStorage.setItem(USER_KEY, JSON.stringify(user))
    set({
      user,
      token,
      isAuthenticated: true,
      isAdmin: user.role === 'ADMIN',
    })
  },

  logout: () => {
    TokenStorage.remove()
    localStorage.removeItem(USER_KEY)
    set({ user: null, token: null, isAuthenticated: false, isAdmin: false })
  },
}))
