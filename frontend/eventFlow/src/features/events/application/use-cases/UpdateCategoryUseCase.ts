import type { CategoryRepository } from '../../domain/ports/CategoryRepository'
import type { Category, UpdateCategoryRequest } from '../../domain/entities/Category'

export class UpdateCategoryUseCase {
  constructor(private readonly categoryRepository: CategoryRepository) {}

  execute(id: number, request: UpdateCategoryRequest): Promise<Category> {
    return this.categoryRepository.update(id, request)
  }
}
