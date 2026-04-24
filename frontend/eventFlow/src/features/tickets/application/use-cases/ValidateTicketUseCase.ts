import type { TicketRepository } from '../../domain/ports/TicketRepository'
import type { Ticket } from '../../domain/entities/Ticket'

export class ValidateTicketUseCase {
  constructor(private readonly ticketRepository: TicketRepository) {}

  execute(qrCode: string): Promise<Ticket> {
    return this.ticketRepository.validate(qrCode)
  }
}
