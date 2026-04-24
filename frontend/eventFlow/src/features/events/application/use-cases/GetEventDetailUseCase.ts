import type { EventRepository } from '../../domain/ports/EventRepository'
import type { EventWithTicketTypes } from '../../domain/entities/Event'

export class GetEventDetailUseCase {
  constructor(private readonly eventRepository: EventRepository) {}

  execute(id: string): Promise<EventWithTicketTypes> {
    return this.eventRepository.getById(id)
  }
}
