import type { CategoryRepository } from '../../domain/ports/CategoryRepository'
import type { Category } from '../../domain/entities/Category'

export class GetCategoryUseCase {
  constructor(private readonly categoryRepository: CategoryRepository) {}

  execute(id: number): Promise<Category> {
    return this.categoryRepository.getById(id)
  }
}
