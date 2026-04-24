import type { Event, EventWithTicketTypes } from '../entities/Event'
import type { Category } from '../entities/Category'
import type { PaginatedResponse } from '@/shared/types/pagination.types'

export interface EventFilters {
  categoryId?: number
  page?: number
  size?: number
}

export interface EventRepository {
  list(filters: EventFilters): Promise<PaginatedResponse<Event>>
  getById(id: string): Promise<EventWithTicketTypes>
  listCategories(): Promise<Category[]>
}
