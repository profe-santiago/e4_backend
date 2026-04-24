import { useMutation, useQueryClient } from '@tanstack/react-query'
import toast from 'react-hot-toast'
import { useOrderRepository } from '@/core/di/OrderContext'
import { CancelOrderUseCase } from '../../application/use-cases/CancelOrderUseCase'
import { RefundOrderUseCase } from '../../application/use-cases/RefundOrderUseCase'

export const useOrderActions = (orderId: string) => {
  const queryClient = useQueryClient()
  const orderRepository = useOrderRepository()

  const invalidate = () => {
    void queryClient.invalidateQueries({ queryKey: ['order', orderId] })
    void queryClient.invalidateQueries({ queryKey: ['orders'] })
  }

  const cancel = useMutation({
    mutationFn: () => new CancelOrderUseCase(orderRepository).execute(orderId),
    onSuccess: () => { invalidate(); toast.success('Orden cancelada') },
    onError: () => toast.error('No se pudo cancelar la orden'),
  })

  const refund = useMutation({
    mutationFn: () => new RefundOrderUseCase(orderRepository).execute(orderId),
    onSuccess: () => { invalidate(); toast.success('Solicitud de reembolso enviada') },
    onError: () => toast.error('No se pudo solicitar el reembolso'),
  })

  return { cancel, refund }
}
