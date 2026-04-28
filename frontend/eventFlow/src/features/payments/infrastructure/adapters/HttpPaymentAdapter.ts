import type { AxiosInstance } from 'axios'
import type { PaymentRepository } from '../../domain/ports/PaymentRepository'
import type { Payment } from '../../domain/entities/Payment'

export class HttpPaymentAdapter implements PaymentRepository {
  constructor(private readonly client: AxiosInstance) {}

  async getById(id: string): Promise<Payment> {
    const { data } = await this.client.get<Payment>(`/api/v1/payments/${id}`)
    return data
  }

  async getByOrderId(orderId: string): Promise<Payment> {
    const { data } = await this.client.get<Payment>(`/api/v1/payments/order/${orderId}`)
    return data
  }
}
