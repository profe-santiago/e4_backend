import { useMutation, useQueryClient } from '@tanstack/react-query'
import { toast } from 'react-hot-toast'
import { useCategoryRepository } from '@/core/di/CategoryContext'
import { CreateCategoryUseCase } from '../../application/use-cases/CreateCategoryUseCase'
import { UpdateCategoryUseCase } from '../../application/use-cases/UpdateCategoryUseCase'
import { DeleteCategoryUseCase } from '../../application/use-cases/DeleteCategoryUseCase'
import type { CreateCategoryRequest, UpdateCategoryRequest } from '../../domain/entities/Category'

export const useCategoryActions = () => {
  const queryClient = useQueryClient()
  const categoryRepository = useCategoryRepository()

  const invalidate = () => {
    queryClient.invalidateQueries({ queryKey: ['categories'] })
  }

  const createCategory = useMutation({
    mutationFn: (request: CreateCategoryRequest) =>
      new CreateCategoryUseCase(categoryRepository).execute(request),
    onSuccess: () => {
      invalidate()
      toast.success('Categoría creada.')
    },
    onError: () => toast.error('Error al crear la categoría. Intentá de nuevo.'),
  })

  const updateCategory = useMutation({
    mutationFn: ({ id, request }: { id: number; request: UpdateCategoryRequest }) =>
      new UpdateCategoryUseCase(categoryRepository).execute(id, request),
    onSuccess: () => {
      invalidate()
      toast.success('Categoría actualizada.')
    },
    onError: () => toast.error('Error al actualizar la categoría. Intentá de nuevo.'),
  })

  const deleteCategory = useMutation({
    mutationFn: (id: number) =>
      new DeleteCategoryUseCase(categoryRepository).execute(id),
    onSuccess: () => {
      invalidate()
      toast.success('Categoría eliminada.')
    },
    onError: () => toast.error('Error al eliminar la categoría. Intentá de nuevo.'),
  })

  return { createCategory, updateCategory, deleteCategory }
}
