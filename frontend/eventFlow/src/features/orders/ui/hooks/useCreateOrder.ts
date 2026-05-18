import { useMutation, useQueryClient } from '@tanstack/react-query'
import { useNavigate } from 'react-router-dom'
import toast from 'react-hot-toast'
import { useOrderRepository } from '@/core/di/OrderContext'
import { CreateOrderUseCase } from '../../application/use-cases/CreateOrderUseCase'
import type { CreateOrderRequest } from '../../domain/entities/Order'

export const useCreateOrder = () => {
  const navigate = useNavigate()
  const queryClient = useQueryClient()
  const orderRepository = useOrderRepository()

  return useMutation({
    mutationFn: (request: CreateOrderRequest) =>
      new CreateOrderUseCase(orderRepository).execute(request),
    onSuccess: (order) => {
      void queryClient.invalidateQueries({ queryKey: ['orders'] })
      void queryClient.invalidateQueries({ queryKey: ['tickets'] })
      navigate(`/orders/${order.id}`)
    },
    onError: () => {
      toast.error(
        'Hubo un problema al procesar tu compra. Revisa "Mis órdenes" antes de intentar de nuevo — es posible que el pago ya se haya realizado.',
        { duration: 8000 },
      )
    },
  })
}
