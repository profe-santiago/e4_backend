import { useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { useNotificationRepository } from '@/core/di/NotificationContext'
import { useAuthStore } from '@/store/auth.store'
import { GetMyNotificationsUseCase } from '../../application/use-cases/GetMyNotificationsUseCase'

export const useMyNotifications = () => {
  const [page, setPage] = useState(0)
  const notificationRepository = useNotificationRepository()
  const userId = useAuthStore((s) => s.user?.userId)

  const query = useQuery({
    queryKey: ['notifications', userId, page],
    queryFn: () => new GetMyNotificationsUseCase(notificationRepository).execute(userId!, page, 20),
    enabled: !!userId,
  })

  return { ...query, page, onPageChange: setPage }
}
