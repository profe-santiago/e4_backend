import { create } from 'zustand'
import { TokenStorage } from '@/core/storage/TokenStorage'

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

export const useAuthStore = create<AuthState>((set) => ({
  user: null,
  token: TokenStorage.get(),
  isAuthenticated: !!TokenStorage.get(),
  isAdmin: false,

  setAuth: (user, token) => {
    TokenStorage.set(token)
    set({
      user,
      token,
      isAuthenticated: true,
      isAdmin: user.role === 'ADMIN',
    })
  },

  logout: () => {
    TokenStorage.remove()
    set({ user: null, token: null, isAuthenticated: false, isAdmin: false })
  },
}))
