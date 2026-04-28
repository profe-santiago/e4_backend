import type { EventRepository } from '../../domain/ports/EventRepository'
import type { Event } from '../../domain/entities/Event'

export class GetMyEventsUseCase {
  constructor(private readonly eventRepository: EventRepository) {}

  execute(): Promise<Event[]> {
    return this.eventRepository.getMyEvents()
  }
}
