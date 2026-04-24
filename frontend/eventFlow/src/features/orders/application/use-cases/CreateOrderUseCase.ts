import type { OrderRepository } from '../../domain/ports/OrderRepository'
import type { Order, CreateOrderRequest } from '../../domain/entities/Order'

export class CreateOrderUseCase {
  constructor(private readonly orderRepository: OrderRepository) {}

  execute(request: CreateOrderRequest): Promise<Order> {
    return this.orderRepository.create(request)
  }
}
