import type { EventRepository } from '../../domain/ports/EventRepository'

export class DeleteEventUseCase {
  constructor(private readonly eventRepository: EventRepository) {}

  execute(id: string): Promise<void> {
    return this.eventRepository.deleteEvent(id)
  }
}
