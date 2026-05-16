import type { AxiosInstance } from 'axios'
import type { NotificationRepository } from '../../domain/ports/NotificationRepository'
import type { Notification } from '../../domain/entities/Notification'
import type { PaginatedResponse } from '@/shared/types/pagination.types'

export class HttpNotificationAdapter implements NotificationRepository {
  constructor(private readonly client: AxiosInstance) {}

  async getByUser(userId: string, page: number, size: number): Promise<PaginatedResponse<Notification>> {
    const { data } = await this.client.get<PaginatedResponse<Notification>>(
      `/api/v1/notifications/users/${userId}`,
      { params: { page, size } },
    )
    return data
  }
}
