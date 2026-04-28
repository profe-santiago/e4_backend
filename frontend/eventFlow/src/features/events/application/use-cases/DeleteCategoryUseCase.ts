import type { CategoryRepository } from '../../domain/ports/CategoryRepository'

export class DeleteCategoryUseCase {
  constructor(private readonly categoryRepository: CategoryRepository) {}

  execute(id: number): Promise<void> {
    return this.categoryRepository.delete(id)
  }
}
