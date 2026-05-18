import { createContext, useContext, type ReactNode } from 'react'
import { apiClient } from '@/core/http/axios.instance'
import { HttpNotificationAdapter } from '@/features/notifications/infrastructure/adapters/HttpNotificationAdapter'
import type { NotificationRepository } from '@/features/notifications/domain/ports/NotificationRepository'

const NotificationRepositoryCtx = createContext<NotificationRepository | null>(null)

const notificationAdapter = new HttpNotificationAdapter(apiClient)

export const NotificationContextProvider = ({ children }: { children: ReactNode }) => (
  <NotificationRepositoryCtx.Provider value={notificationAdapter}>
    {children}
  </NotificationRepositoryCtx.Provider>
)

export const useNotificationRepository = (): NotificationRepository => {
  const ctx = useContext(NotificationRepositoryCtx)
  if (!ctx) throw new Error('useNotificationRepository must be used inside NotificationContextProvider')
  return ctx
}
