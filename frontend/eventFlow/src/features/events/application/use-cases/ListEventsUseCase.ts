import type { EventRepository, EventFilters } from '../../domain/ports/EventRepository'
import type { PaginatedResponse } from '@/shared/types/pagination.types'
import type { Event } from '../../domain/entities/Event'

export class ListEventsUseCase {
  constructor(private readonly eventRepository: EventRepository) {}

  execute(filters: EventFilters): Promise<PaginatedResponse<Event>> {
    return this.eventRepository.list(filters)
  }
}
