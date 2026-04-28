import { createContext, useContext, type ReactNode } from 'react'
import { apiClient } from '@/core/http/axios.instance'
import { HttpUserAdapter } from '@/features/profile/infrastructure/adapters/HttpUserAdapter'
import type { UserRepository } from '@/features/profile/domain/ports/UserRepository'

const UserRepositoryCtx = createContext<UserRepository | null>(null)

const userAdapter = new HttpUserAdapter(apiClient)

export const UserContextProvider = ({ children }: { children: ReactNode }) => (
  <UserRepositoryCtx.Provider value={userAdapter}>
    {children}
  </UserRepositoryCtx.Provider>
)

export const useUserRepository = (): UserRepository => {
  const ctx = useContext(UserRepositoryCtx)
  if (!ctx) throw new Error('useUserRepository must be used inside UserContextProvider')
  return ctx
}
