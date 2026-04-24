import { useMutation } from '@tanstack/react-query'
import { useNavigate } from 'react-router-dom'
import toast from 'react-hot-toast'
import { useOrderRepository } from '@/core/di/OrderContext'
import { CreateOrderUseCase } from '../../application/use-cases/CreateOrderUseCase'
import type { CreateOrderRequest } from '../../domain/entities/Order'

export const useCreateOrder = () => {
  const navigate = useNavigate()
  const orderRepository = useOrderRepository()

  return useMutation({
    mutationFn: (request: CreateOrderRequest) =>
      new CreateOrderUseCase(orderRepository).execute(request),
    onSuccess: (order) => navigate(`/orders/${order.id}`),
    onError: () => toast.error('Error al crear la orden. Intentá de nuevo.'),
  })
}
