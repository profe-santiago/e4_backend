import type { TicketTypeRepository } from '../../domain/ports/TicketTypeRepository'

export class DeleteTicketTypeUseCase {
  constructor(private readonly ticketTypeRepository: TicketTypeRepository) {}

  execute(eventId: string, id: number): Promise<void> {
    return this.ticketTypeRepository.delete(eventId, id)
  }
}
