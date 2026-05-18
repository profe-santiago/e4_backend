import type { TicketRepository } from '../../domain/ports/TicketRepository'
import type { Ticket } from '../../domain/entities/Ticket'
import type { PaginatedResponse } from '@/shared/types/pagination.types'

export class GetMyTicketsUseCase {
  constructor(private readonly ticketRepository: TicketRepository) {}

  execute(page: number, size: number): Promise<PaginatedResponse<Ticket>> {
    return this.ticketRepository.getMyTickets(page, size)
  }
}
