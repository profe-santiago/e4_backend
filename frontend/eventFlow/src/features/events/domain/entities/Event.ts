import type { TicketType } from './TicketType'
import type { Category } from './Category'

export type EventStatus = 'DRAFT' | 'PUBLISHED' | 'CANCELLED'

export interface Event {
  id: string
  organizerId: string
  title: string
  description: string
  category: Category | null
  venue: string
  city: string
  country: string
  startDate: string
  endDate: string
  imageUrl: string | null
  status: EventStatus
  createdAt: string
  updatedAt: string
}

export interface EventWithTicketTypes extends Event {
  ticketTypes: TicketType[]
}

export interface CreateEventRequest {
  title: string
  description?: string
  categoryId?: number
  venue: string
  city: string
  country: string
  startDate: string
  endDate?: string
  imageUrl?: string
}

export interface UpdateEventRequest {
  title?: string
  description?: string
  categoryId?: number
  venue?: string
  city?: string
  country?: string
  startDate?: string
  endDate?: string
  imageUrl?: string
}
