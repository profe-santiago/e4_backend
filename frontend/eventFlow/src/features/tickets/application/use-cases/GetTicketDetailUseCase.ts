import type { TicketRepository } from '../../domain/ports/TicketRepository'
import type { Ticket } from '../../domain/entities/Ticket'

export class GetTicketDetailUseCase {
  constructor(private readonly ticketRepository: TicketRepository) {}

  execute(id: string): Promise<Ticket> {
    return this.ticketRepository.getById(id)
  }
}
