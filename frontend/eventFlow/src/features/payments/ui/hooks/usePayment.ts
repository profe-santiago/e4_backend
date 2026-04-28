import { useQuery } from '@tanstack/react-query'
import { usePaymentRepository } from '@/core/di/PaymentContext'
import { GetPaymentUseCase } from '../../application/use-cases/GetPaymentUseCase'

export const usePayment = (id: string) => {
  const paymentRepository = usePaymentRepository()

  return useQuery({
    queryKey: ['payment', id],
    queryFn: () => new GetPaymentUseCase(paymentRepository).execute(id),
    enabled: !!id,
  })
}
