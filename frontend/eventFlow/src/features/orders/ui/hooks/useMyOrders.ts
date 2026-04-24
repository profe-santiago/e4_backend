import { useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { useOrderRepository } from '@/core/di/OrderContext'
import { GetMyOrdersUseCase } from '../../application/use-cases/GetMyOrdersUseCase'

export const useMyOrders = () => {
  const [page, setPage] = useState(0)
  const orderRepository = useOrderRepository()

  const query = useQuery({
    queryKey: ['orders', page],
    queryFn: () => new GetMyOrdersUseCase(orderRepository).execute(page, 10),
  })

  return { ...query, page, onPageChange: setPage }
}
