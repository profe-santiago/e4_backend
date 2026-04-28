import { useNavigate } from 'react-router-dom'
import { useMutation, useQueryClient } from '@tanstack/react-query'
import { toast } from 'react-hot-toast'
import { useEventRepository } from '@/core/di/EventContext'
import { CreateEventUseCase } from '../../application/use-cases/CreateEventUseCase'
import type { CreateEventRequest } from '../../domain/entities/Event'

export const useCreateEvent = () => {
  const navigate = useNavigate()
  const queryClient = useQueryClient()
  const eventRepository = useEventRepository()

  return useMutation({
    mutationFn: (request: CreateEventRequest) =>
      new CreateEventUseCase(eventRepository).execute(request),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['my-events'] })
      queryClient.invalidateQueries({ queryKey: ['events'] })
      navigate('/my-events')
    },
    onError: () => toast.error('Error al crear el evento. Intentá de nuevo.'),
  })
}
