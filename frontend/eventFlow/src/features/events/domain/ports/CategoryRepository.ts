import type { Category, CreateCategoryRequest, UpdateCategoryRequest } from '../entities/Category'

export interface CategoryRepository {
  getById(id: number): Promise<Category>
  create(request: CreateCategoryRequest): Promise<Category>
  update(id: number, request: UpdateCategoryRequest): Promise<Category>
  delete(id: number): Promise<void>
}
