import type { TicketTypeRepository } from '../../domain/ports/TicketTypeRepository'
import type { TicketType, CreateTicketTypeRequest } from '../../domain/entities/TicketType'

export class CreateTicketTypeUseCase {
  constructor(private readonly ticketTypeRepository: TicketTypeRepository) {}

  execute(eventId: string, request: CreateTicketTypeRequest): Promise<TicketType> {
    return this.ticketTypeRepository.create(eventId, request)
  }
}
