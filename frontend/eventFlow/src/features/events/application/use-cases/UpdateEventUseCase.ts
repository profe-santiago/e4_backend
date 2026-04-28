import type { EventRepository } from '../../domain/ports/EventRepository'
import type { Event, UpdateEventRequest } from '../../domain/entities/Event'

export class UpdateEventUseCase {
  constructor(private readonly eventRepository: EventRepository) {}

  execute(id: string, request: UpdateEventRequest): Promise<Event> {
    return this.eventRepository.updateEvent(id, request)
  }
}
