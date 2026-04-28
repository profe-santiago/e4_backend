import { useMutation, useQueryClient } from '@tanstack/react-query'
import { toast } from 'react-hot-toast'
import { useEventRepository } from '@/core/di/EventContext'
import { ChangeEventStatusUseCase } from '../../application/use-cases/ChangeEventStatusUseCase'
import { DeleteEventUseCase } from '../../application/use-cases/DeleteEventUseCase'
import type { EventStatus } from '../../domain/entities/Event'

export const useEventActions = () => {
  const queryClient = useQueryClient()
  const eventRepository = useEventRepository()

  const invalidate = () => {
    queryClient.invalidateQueries({ queryKey: ['my-events'] })
    queryClient.invalidateQueries({ queryKey: ['events'] })
  }

  const changeStatus = useMutation({
    mutationFn: ({ id, status }: { id: string; status: EventStatus }) =>
      new ChangeEventStatusUseCase(eventRepository).execute(id, status),
    onSuccess: () => {
      invalidate()
      toast.success('Estado actualizado.')
    },
    onError: () => toast.error('Error al cambiar el estado. Intentá de nuevo.'),
  })

  const deleteEvent = useMutation({
    mutationFn: (id: string) => new DeleteEventUseCase(eventRepository).execute(id),
    onSuccess: () => {
      invalidate()
      toast.success('Evento eliminado.')
    },
    onError: () => toast.error('Error al eliminar el evento. Intentá de nuevo.'),
  })

  return { changeStatus, deleteEvent }
}
