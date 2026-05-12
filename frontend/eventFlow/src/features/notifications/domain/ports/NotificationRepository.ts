import type { Notification } from '../entities/Notification'
import type { PaginatedResponse } from '@/shared/types/pagination.types'

export interface NotificationRepository {
  getByUser(userId: string, page: number, size: number): Promise<PaginatedResponse<Notification>>
}
