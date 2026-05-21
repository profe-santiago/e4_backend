import { useQuery } from '@tanstack/react-query'
import { useTicketRepository } from '@/core/di/TicketContext'

export const useTicketsByOrder = (orderId: string, enabled = true) => {
  const ticketRepository = useTicketRepository()

  return useQuery({
    queryKey: ['tickets-by-order', orderId],
    queryFn: () => ticketRepository.getByOrderId(orderId),
    enabled: enabled && !!orderId,
  })
}
