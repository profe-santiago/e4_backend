import { useNavigate } from 'react-router-dom'
import { useMutation, useQueryClient } from '@tanstack/react-query'
import { toast } from 'react-hot-toast'
import { useEventRepository } from '@/core/di/EventContext'
import { UpdateEventUseCase } from '../../application/use-cases/UpdateEventUseCase'
import type { UpdateEventRequest } from '../../domain/entities/Event'

export const useUpdateEvent = (id: string) => {
  const navigate = useNavigate()
  const queryClient = useQueryClient()
  const eventRepository = useEventRepository()

  return useMutation({
    mutationFn: (request: UpdateEventRequest) =>
      new UpdateEventUseCase(eventRepository).execute(id, request),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['my-events'] })
      queryClient.invalidateQueries({ queryKey: ['events'] })
      queryClient.invalidateQueries({ queryKey: ['event', id] })
      navigate('/my-events')
    },
    onError: () => toast.error('Error al actualizar el evento. Intentá de nuevo.'),
  })
}
