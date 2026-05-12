import { useQuery } from '@tanstack/react-query'
import { useNotificationRepository } from '@/core/di/NotificationContext'
import { useAuthStore } from '@/store/auth.store'
import { GetMyNotificationsUseCase } from '../../application/use-cases/GetMyNotificationsUseCase'

const STORAGE_KEY = 'notif_last_viewed'

export const markNotificationsViewed = () =>
  localStorage.setItem(STORAGE_KEY, new Date().toISOString())

const getLastViewed = () => localStorage.getItem(STORAGE_KEY) ?? '1970-01-01T00:00:00Z'

export const useUnreadNotificationCount = () => {
  const notificationRepository = useNotificationRepository()
  const userId = useAuthStore((s) => s.user?.userId)

  const { data = 0 } = useQuery({
    queryKey: ['notifications-unread', userId],
    queryFn: async () => {
      const result = await new GetMyNotificationsUseCase(notificationRepository).execute(userId!, 0, 20)
      const lastViewed = getLastViewed()
      const count = result.content.filter(n => (n.sentAt ?? n.createdAt) > lastViewed).length
      return Math.min(count, 99)
    },
    enabled: !!userId,
    staleTime: 30_000,
    refetchInterval: 60_000,
  })

  return data
}
