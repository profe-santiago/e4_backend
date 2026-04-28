import { createContext, useContext, type ReactNode } from 'react'
import { apiClient } from '@/core/http/axios.instance'
import { HttpCategoryAdapter } from '@/features/events/infrastructure/adapters/HttpCategoryAdapter'
import type { CategoryRepository } from '@/features/events/domain/ports/CategoryRepository'

const CategoryRepositoryCtx = createContext<CategoryRepository | null>(null)

const categoryAdapter = new HttpCategoryAdapter(apiClient)

export const CategoryContextProvider = ({ children }: { children: ReactNode }) => (
  <CategoryRepositoryCtx.Provider value={categoryAdapter}>
    {children}
  </CategoryRepositoryCtx.Provider>
)

export const useCategoryRepository = (): CategoryRepository => {
  const ctx = useContext(CategoryRepositoryCtx)
  if (!ctx) throw new Error('useCategoryRepository must be used inside CategoryContextProvider')
  return ctx
}
