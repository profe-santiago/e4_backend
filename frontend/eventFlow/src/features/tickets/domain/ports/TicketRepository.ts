import type { Ticket } from '../entities/Ticket'
import type { PaginatedResponse } from '@/shared/types/pagination.types'

export interface TicketRepository {
  getMyTickets(page: number, size: number): Promise<PaginatedResponse<Ticket>>
  getById(id: string): Promise<Ticket>
  validate(qrCode: string): Promise<Ticket>
}
