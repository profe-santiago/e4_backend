import type { Event, EventWithTicketTypes, CreateEventRequest, UpdateEventRequest, EventStatus } from '../entities/Event'
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
  getMyEvents(): Promise<Event[]>
  createEvent(request: CreateEventRequest): Promise<Event>
  updateEvent(id: string, request: UpdateEventRequest): Promise<Event>
  changeEventStatus(id: string, status: EventStatus): Promise<Event>
  deleteEvent(id: string): Promise<void>
}
