import type { AxiosInstance } from 'axios'
import type { CategoryRepository } from '../../domain/ports/CategoryRepository'
import type { Category, CreateCategoryRequest, UpdateCategoryRequest } from '../../domain/entities/Category'

export class HttpCategoryAdapter implements CategoryRepository {
  constructor(private readonly client: AxiosInstance) {}

  async getById(id: number): Promise<Category> {
    const { data } = await this.client.get<Category>(`/api/v1/categories/${id}`)
    return data
  }

  async create(request: CreateCategoryRequest): Promise<Category> {
    const { data } = await this.client.post<Category>('/api/v1/categories', request)
    return data
  }

  async update(id: number, request: UpdateCategoryRequest): Promise<Category> {
    const { data } = await this.client.put<Category>(`/api/v1/categories/${id}`, request)
    return data
  }

  async delete(id: number): Promise<void> {
    await this.client.delete(`/api/v1/categories/${id}`)
  }
}
