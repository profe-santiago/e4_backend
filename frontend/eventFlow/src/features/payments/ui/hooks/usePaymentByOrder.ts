import { useQuery } from '@tanstack/react-query'
import { usePaymentRepository } from '@/core/di/PaymentContext'
import { GetPaymentByOrderUseCase } from '../../application/use-cases/GetPaymentByOrderUseCase'

export const usePaymentByOrder = (orderId: string, enabled = true) => {
  const paymentRepository = usePaymentRepository()

  return useQuery({
    queryKey: ['payment-order', orderId],
    queryFn: () => new GetPaymentByOrderUseCase(paymentRepository).execute(orderId),
    enabled: enabled && !!orderId,
    retry: 0,
    refetchInterval: (query) => {
      const status = query.state.data?.status
      if (status === 'APPROVED' || status === 'REJECTED' || status === 'REFUNDED') return false
      return 3000
    },
  })
}
