import type { AxiosInstance } from 'axios'
import type { OrderRepository } from '../../domain/ports/OrderRepository'
import type { Order, CreateOrderRequest } from '../../domain/entities/Order'
import type { PaginatedResponse } from '@/shared/types/pagination.types'

export class HttpOrderAdapter implements OrderRepository {
  constructor(private readonly client: AxiosInstance) {}

  async create(request: CreateOrderRequest): Promise<Order> {
    const { data } = await this.client.post<Order>('/api/v1/orders', request)
    return data
  }

  async getById(id: string): Promise<Order> {
    const { data } = await this.client.get<Order>(`/api/v1/orders/${id}`)
    return data
  }

  async getMyOrders(page: number, size: number): Promise<PaginatedResponse<Order>> {
    const { data } = await this.client.get<PaginatedResponse<Order>>('/api/v1/orders/my', {
      params: { page, size },
    })
    return data
  }

  async cancel(id: string): Promise<Order> {
    const { data } = await this.client.patch<Order>(`/api/v1/orders/${id}/cancel`)
    return data
  }

  async refund(id: string): Promise<Order> {
    const { data } = await this.client.patch<Order>(`/api/v1/orders/${id}/refund`)
    return data
  }
}
