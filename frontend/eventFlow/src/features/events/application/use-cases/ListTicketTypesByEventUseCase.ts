import type { TicketTypeRepository } from '../../domain/ports/TicketTypeRepository'
import type { TicketType } from '../../domain/entities/TicketType'

export class ListTicketTypesByEventUseCase {
  constructor(private readonly ticketTypeRepository: TicketTypeRepository) {}

  execute(eventId: string): Promise<TicketType[]> {
    return this.ticketTypeRepository.listByEvent(eventId)
  }
}
