import type { AxiosInstance } from 'axios'
import type { EventRepository, EventFilters } from '../../domain/ports/EventRepository'
import type { Event, EventWithTicketTypes, CreateEventRequest, UpdateEventRequest, EventStatus } from '../../domain/entities/Event'
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
        ...(filters.search    ? { search: filters.search }           : {}),
        ...(filters.city      ? { city: filters.city }               : {}),
        ...(filters.venue     ? { venue: filters.venue }             : {}),
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

  async getMyEvents(): Promise<Event[]> {
    const { data } = await this.client.get<Event[]>('/api/v1/events/my')
    return data
  }

  async uploadImage(file: File): Promise<string> {
    const formData = new FormData()
    formData.append('file', file)
    const { data } = await this.client.post<{ imageUrl: string }>('/api/v1/upload/image', formData)
    return data.imageUrl
  }

  async createEvent(request: CreateEventRequest): Promise<Event> {
    const { data } = await this.client.post<Event>('/api/v1/events', request)
    return data
  }

  async updateEvent(id: string, request: UpdateEventRequest): Promise<Event> {
    const { data } = await this.client.put<Event>(`/api/v1/events/${id}`, request)
    return data
  }

  async changeEventStatus(id: string, status: EventStatus): Promise<Event> {
    const { data } = await this.client.patch<Event>(`/api/v1/events/${id}/status`, null, {
      params: { status },
    })
    return data
  }

  async deleteEvent(id: string): Promise<void> {
    await this.client.delete(`/api/v1/events/${id}`)
  }
}
