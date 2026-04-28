import type { CategoryRepository } from '../../domain/ports/CategoryRepository'
import type { Category, CreateCategoryRequest } from '../../domain/entities/Category'

export class CreateCategoryUseCase {
  constructor(private readonly categoryRepository: CategoryRepository) {}

  execute(request: CreateCategoryRequest): Promise<Category> {
    return this.categoryRepository.create(request)
  }
}
