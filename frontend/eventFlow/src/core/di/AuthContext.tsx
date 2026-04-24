import { createContext, useContext, type ReactNode } from 'react'
import { apiClient } from '@/core/http/axios.instance'
import { HttpAuthAdapter } from '@/features/auth/infrastructure/adapters/HttpAuthAdapter'
import { HttpUserCreationAdapter } from '@/features/auth/infrastructure/adapters/HttpUserCreationAdapter'
import type { AuthRepository } from '@/features/auth/domain/ports/AuthRepository'
import type { UserCreationPort } from '@/features/auth/domain/ports/UserCreationPort'

const AuthRepositoryCtx = createContext<AuthRepository | null>(null)
const UserCreationCtx = createContext<UserCreationPort | null>(null)

const authAdapter = new HttpAuthAdapter(apiClient)
const userCreationAdapter = new HttpUserCreationAdapter(apiClient)

export const AuthContextProvider = ({ children }: { children: ReactNode }) => (
  <AuthRepositoryCtx.Provider value={authAdapter}>
    <UserCreationCtx.Provider value={userCreationAdapter}>
      {children}
    </UserCreationCtx.Provider>
  </AuthRepositoryCtx.Provider>
)

export const useAuthRepository = (): AuthRepository => {
  const ctx = useContext(AuthRepositoryCtx)
  if (!ctx) throw new Error('useAuthRepository must be used inside AuthContextProvider')
  return ctx
}

export const useUserCreationPort = (): UserCreationPort => {
  const ctx = useContext(UserCreationCtx)
  if (!ctx) throw new Error('useUserCreationPort must be used inside AuthContextProvider')
  return ctx
}
