import { useEffect, useRef } from 'react'
import { useQuery, useQueryClient } from '@tanstack/react-query'
import { useOrderRepository } from '@/core/di/OrderContext'
import { GetOrderUseCase } from '../../application/use-cases/GetOrderUseCase'

export const useOrderDetail = (id: string) => {
  const orderRepository = useOrderRepository()
  const queryClient = useQueryClient()
  const prevStatusRef = useRef<string | undefined>(undefined)

  const query = useQuery({
    queryKey: ['order', id],
    queryFn: () => new GetOrderUseCase(orderRepository).execute(id),
    enabled: !!id,
    refetchInterval: (q) => {
      const status = q.state.data?.status
      return status === 'PENDING' ? 3000 : false
    },
  })

  useEffect(() => {
    const status = query.data?.status
    if (prevStatusRef.current === 'PENDING' && status && status !== 'PENDING') {
      void queryClient.invalidateQueries({ queryKey: ['orders'] })
    }
    prevStatusRef.current = status
  }, [query.data?.status, queryClient])

  return query
}
