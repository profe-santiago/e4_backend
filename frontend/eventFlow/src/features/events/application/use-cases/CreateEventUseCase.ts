import type { EventRepository } from '../../domain/ports/EventRepository'
import type { Event, CreateEventRequest } from '../../domain/entities/Event'

export class CreateEventUseCase {
  constructor(private readonly eventRepository: EventRepository) {}

  execute(request: CreateEventRequest): Promise<Event> {
    return this.eventRepository.createEvent(request)
  }
}
