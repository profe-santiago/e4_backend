import type { AxiosInstance } from 'axios'
import type { EventRepository, EventFilters } from '../../domain/ports/EventRepository'
import type { Event, EventWithTicketTypes } from '../../domain/entities/Event'
import type { Category } from '../../domain/entities/Category'
import type { PaginatedResponse } from '@/shared/types/pagination.types'

export class HttpEventAdapter implements EventRepository {
  constructor(private readonly client: AxiosInstance) {}

  async list(filters: EventFilters): Promise<PaginatedResponse<Event>> {
    const { data } = await this.client.get<PaginatedResponse<Event>>('/api/v1/events', {
      params: {
        page: filters.page ?? 0,
        size: filters.size ?? 12,
        ...(filters.categoryId ? { categoryId: filters.categoryId } : {}),
      },
    })
    return data
  }

  async getById(id: string): Promise<EventWithTicketTypes> {
    const { data } = await this.client.get<EventWithTicketTypes>(`/api/v1/events/${id}`)
    return data
  }

  async listCategories(): Promise<Category[]> {
    const { data } = await this.client.get<Category[]>('/api/v1/categories')
    return data
  }
}
