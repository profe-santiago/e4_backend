export interface Category {
  id: number
  name: string
  description: string | null
}

export interface CreateCategoryRequest {
  name: string
  description?: string
}

export interface UpdateCategoryRequest {
  name: string
  description?: string
}
