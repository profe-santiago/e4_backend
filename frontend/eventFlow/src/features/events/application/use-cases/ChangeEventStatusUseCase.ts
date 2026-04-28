import type { EventRepository } from '../../domain/ports/EventRepository'
import type { Event, EventStatus } from '../../domain/entities/Event'

export class ChangeEventStatusUseCase {
  constructor(private readonly eventRepository: EventRepository) {}

  execute(id: string, status: EventStatus): Promise<Event> {
    return this.eventRepository.changeEventStatus(id, status)
  }
}
