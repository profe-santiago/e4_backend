import { useQuery } from '@tanstack/react-query'
import { useEventRepository } from '@/core/di/EventContext'
import { GetMyEventsUseCase } from '../../application/use-cases/GetMyEventsUseCase'

export const useMyEvents = () => {
  const eventRepository = useEventRepository()

  return useQuery({
    queryKey: ['my-events'],
    queryFn: () => new GetMyEventsUseCase(eventRepository).execute(),
  })
}
