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

const storedToken = TokenStorage.get()
const storedUser = storedToken ? loadUser() : null

export const useAuthStore = create<AuthState>((set) => ({
  user: storedUser,
  token: storedToken,
  isAuthenticated: !!storedToken,
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
