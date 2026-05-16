import { useMutation } from '@tanstack/react-query'
import { usePaymentRepository } from '@/core/di/PaymentContext'

export const useCreatePaymentIntent = () => {
  const repo = usePaymentRepository()
  return useMutation({
    mutationFn: ({ amount, currency }: { amount: number; currency: string }) =>
      repo.createIntent(amount, currency),
  })
}
