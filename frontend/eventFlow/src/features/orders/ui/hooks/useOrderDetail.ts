import { useQuery } from '@tanstack/react-query'
import { useOrderRepository } from '@/core/di/OrderContext'
import { GetOrderUseCase } from '../../application/use-cases/GetOrderUseCase'

export const useOrderDetail = (id: string) => {
  const orderRepository = useOrderRepository()

  return useQuery({
    queryKey: ['order', id],
    queryFn: () => new GetOrderUseCase(orderRepository).execute(id),
    enabled: !!id,
    refetchInterval: (query) => {
      const status = query.state.data?.status
      return status === 'PENDING' ? 3000 : false
    },
  })
}
