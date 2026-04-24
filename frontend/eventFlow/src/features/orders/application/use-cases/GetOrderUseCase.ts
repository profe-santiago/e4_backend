import type { OrderRepository } from '../../domain/ports/OrderRepository'
import type { Order } from '../../domain/entities/Order'

export class GetOrderUseCase {
  constructor(private readonly orderRepository: OrderRepository) {}

  execute(id: string): Promise<Order> {
    return this.orderRepository.getById(id)
  }
}
