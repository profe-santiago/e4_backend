import type { TicketType } from './TicketType'

export type EventStatus = 'DRAFT' | 'PUBLISHED' | 'CANCELLED'

export interface Event {
  id: string
  organizerId: string
  title: string
  description: string
  category: string
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
