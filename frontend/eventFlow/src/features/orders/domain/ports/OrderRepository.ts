import type { Order, CreateOrderRequest } from '../entities/Order'
import type { PaginatedResponse } from '@/shared/types/pagination.types'

export interface OrderRepository {
  create(request: CreateOrderRequest): Promise<Order>
  getById(id: string): Promise<Order>
  getMyOrders(page: number, size: number): Promise<PaginatedResponse<Order>>
  cancel(id: string): Promise<Order>
  refund(id: string): Promise<Order>
}
