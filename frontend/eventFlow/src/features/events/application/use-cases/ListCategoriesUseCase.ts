import type { EventRepository } from '../../domain/ports/EventRepository'
import type { Category } from '../../domain/entities/Category'

export class ListCategoriesUseCase {
  constructor(private readonly eventRepository: EventRepository) {}

  execute(): Promise<Category[]> {
    return this.eventRepository.listCategories()
  }
}
