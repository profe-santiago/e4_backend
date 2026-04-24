import { useQuery } from '@tanstack/react-query'
import { useEventRepository } from '@/core/di/EventContext'
import { GetEventDetailUseCase } from '../../application/use-cases/GetEventDetailUseCase'

export const useEventDetail = (id: string) => {
  const eventRepository = useEventRepository()

  return useQuery({
    queryKey: ['event', id],
    queryFn: () => new GetEventDetailUseCase(eventRepository).execute(id),
    enabled: !!id,
  })
}
