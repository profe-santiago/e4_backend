import type { OrderRepository } from '../../domain/ports/OrderRepository'
import type { Order } from '../../domain/entities/Order'
import type { PaginatedResponse } from '@/shared/types/pagination.types'

export class GetMyOrdersUseCase {
  constructor(private readonly orderRepository: OrderRepository) {}

  execute(page: number, size: number): Promise<PaginatedResponse<Order>> {
    return this.orderRepository.getMyOrders(page, size)
  }
}
