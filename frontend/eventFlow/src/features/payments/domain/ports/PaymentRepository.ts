import type { Payment } from '../entities/Payment'

export interface PaymentRepository {
  getById(id: string): Promise<Payment>
  getByOrderId(orderId: string): Promise<Payment>
}
