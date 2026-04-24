import { useQuery } from '@tanstack/react-query'
import { useTicketRepository } from '@/core/di/TicketContext'
import { GetTicketDetailUseCase } from '../../application/use-cases/GetTicketDetailUseCase'

export const useTicketDetail = (id: string) => {
  const ticketRepository = useTicketRepository()

  return useQuery({
    queryKey: ['ticket', id],
    queryFn: () => new GetTicketDetailUseCase(ticketRepository).execute(id),
    enabled: !!id,
  })
}
