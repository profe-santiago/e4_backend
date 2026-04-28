import type { TicketType, CreateTicketTypeRequest, UpdateTicketTypeRequest } from '../entities/TicketType'

export interface TicketTypeRepository {
  listByEvent(eventId: string): Promise<TicketType[]>
  create(eventId: string, request: CreateTicketTypeRequest): Promise<TicketType>
  update(eventId: string, id: number, request: UpdateTicketTypeRequest): Promise<TicketType>
  delete(eventId: string, id: number): Promise<void>
}
