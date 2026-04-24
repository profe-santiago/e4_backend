import { useQuery } from '@tanstack/react-query'
import { useEventRepository } from '@/core/di/EventContext'
import { ListCategoriesUseCase } from '../../application/use-cases/ListCategoriesUseCase'

export const useCategories = () => {
  const eventRepository = useEventRepository()

  return useQuery({
    queryKey: ['categories'],
    queryFn: () => new ListCategoriesUseCase(eventRepository).execute(),
    staleTime: Infinity,
  })
}
