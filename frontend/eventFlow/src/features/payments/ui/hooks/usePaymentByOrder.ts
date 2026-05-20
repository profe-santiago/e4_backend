import { useRef } from 'react'
import { useQuery } from '@tanstack/react-query'
import { usePaymentRepository } from '@/core/di/PaymentContext'
import { GetPaymentByOrderUseCase } from '../../application/use-cases/GetPaymentByOrderUseCase'

const TERMINAL_STATUSES = ['APPROVED', 'REJECTED', 'REFUNDED']
const MAX_POLL_ATTEMPTS = 40 // 40 × 3s = 2 minutos

export const usePaymentByOrder = (orderId: string, enabled = true) => {
  const paymentRepository = usePaymentRepository()
  const pollAttempts = useRef(0)

  return useQuery({
    queryKey: ['payment-order', orderId],
    queryFn: async () => {
      pollAttempts.current += 1
      return new GetPaymentByOrderUseCase(paymentRepository).execute(orderId)
    },
    enabled: enabled && !!orderId,
    retry: 0,
    refetchInterval: (query) => {
      const status = query.state.data?.status
      if (status && TERMINAL_STATUSES.includes(status)) return false
      if (pollAttempts.current >= MAX_POLL_ATTEMPTS) return false
      return 3000
    },
  })
}
