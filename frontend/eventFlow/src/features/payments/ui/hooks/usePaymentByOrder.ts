import { useQuery } from '@tanstack/react-query'
import { usePaymentRepository } from '@/core/di/PaymentContext'
import { GetPaymentByOrderUseCase } from '../../application/use-cases/GetPaymentByOrderUseCase'

export const usePaymentByOrder = (orderId: string, enabled = true) => {
  const paymentRepository = usePaymentRepository()

  return useQuery({
    queryKey: ['payment-order', orderId],
    queryFn: () => new GetPaymentByOrderUseCase(paymentRepository).execute(orderId),
    enabled: enabled && !!orderId,
    refetchInterval: (query) => {
      const status = query.state.data?.status
      return status === 'PENDING' || status === undefined ? 3000 : false
    },
  })
}
