import { useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { useTicketRepository } from '@/core/di/TicketContext'
import { GetMyTicketsUseCase } from '../../application/use-cases/GetMyTicketsUseCase'

export const useMyTickets = () => {
  const [page, setPage] = useState(0)
  const ticketRepository = useTicketRepository()

  const query = useQuery({
    queryKey: ['tickets', page],
    queryFn: () => new GetMyTicketsUseCase(ticketRepository).execute(page, 10),
  })

  return { ...query, page, onPageChange: setPage }
}
