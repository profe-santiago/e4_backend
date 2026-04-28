import { useQuery } from '@tanstack/react-query'
import { useTicketTypeRepository } from '@/core/di/TicketTypeContext'
import { ListTicketTypesByEventUseCase } from '../../application/use-cases/ListTicketTypesByEventUseCase'

export const useTicketTypesByEvent = (eventId: string) => {
  const ticketTypeRepository = useTicketTypeRepository()

  return useQuery({
    queryKey: ['ticket-types', eventId],
    queryFn: () => new ListTicketTypesByEventUseCase(ticketTypeRepository).execute(eventId),
    enabled: !!eventId,
  })
}
