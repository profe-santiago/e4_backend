import type { TicketTypeRepository } from '../../domain/ports/TicketTypeRepository'
import type { TicketType, UpdateTicketTypeRequest } from '../../domain/entities/TicketType'

export class UpdateTicketTypeUseCase {
  constructor(private readonly ticketTypeRepository: TicketTypeRepository) {}

  execute(eventId: string, id: number, request: UpdateTicketTypeRequest): Promise<TicketType> {
    return this.ticketTypeRepository.update(eventId, id, request)
  }
}
