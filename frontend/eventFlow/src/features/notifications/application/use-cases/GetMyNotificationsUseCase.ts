import type { NotificationRepository } from '../../domain/ports/NotificationRepository'
import type { Notification } from '../../domain/entities/Notification'
import type { PaginatedResponse } from '@/shared/types/pagination.types'

export class GetMyNotificationsUseCase {
  constructor(private readonly notificationRepository: NotificationRepository) {}

  execute(userId: string, page: number, size: number): Promise<PaginatedResponse<Notification>> {
    return this.notificationRepository.getByUser(userId, page, size)
  }
}
