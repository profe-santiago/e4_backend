import { useMutation, useQueryClient } from '@tanstack/react-query'
import { toast } from 'react-hot-toast'
import { useTicketTypeRepository } from '@/core/di/TicketTypeContext'
import { CreateTicketTypeUseCase } from '../../application/use-cases/CreateTicketTypeUseCase'
import { UpdateTicketTypeUseCase } from '../../application/use-cases/UpdateTicketTypeUseCase'
import { DeleteTicketTypeUseCase } from '../../application/use-cases/DeleteTicketTypeUseCase'
import type { CreateTicketTypeRequest, UpdateTicketTypeRequest } from '../../domain/entities/TicketType'

export const useTicketTypeActions = (eventId: string) => {
  const queryClient = useQueryClient()
  const ticketTypeRepository = useTicketTypeRepository()

  const invalidate = () => {
    queryClient.invalidateQueries({ queryKey: ['ticket-types', eventId] })
    queryClient.invalidateQueries({ queryKey: ['event', eventId] })
  }

  const createTicketType = useMutation({
    mutationFn: (request: CreateTicketTypeRequest) =>
      new CreateTicketTypeUseCase(ticketTypeRepository).execute(eventId, request),
    onSuccess: () => {
      invalidate()
      toast.success('Tipo de ticket creado.')
    },
    onError: () => toast.error('Error al crear el tipo de ticket. Intentá de nuevo.'),
  })

  const updateTicketType = useMutation({
    mutationFn: ({ id, request }: { id: number; request: UpdateTicketTypeRequest }) =>
      new UpdateTicketTypeUseCase(ticketTypeRepository).execute(eventId, id, request),
    onSuccess: () => {
      invalidate()
      toast.success('Tipo de ticket actualizado.')
    },
    onError: () => toast.error('Error al actualizar el tipo de ticket. Intentá de nuevo.'),
  })

  const deleteTicketType = useMutation({
    mutationFn: (id: number) =>
      new DeleteTicketTypeUseCase(ticketTypeRepository).execute(eventId, id),
    onSuccess: () => {
      invalidate()
      toast.success('Tipo de ticket eliminado.')
    },
    onError: () => toast.error('Error al eliminar el tipo de ticket. Intentá de nuevo.'),
  })

  return { createTicketType, updateTicketType, deleteTicketType }
}
