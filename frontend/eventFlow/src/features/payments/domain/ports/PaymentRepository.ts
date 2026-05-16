import type { Payment } from '../entities/Payment'

export interface CreateIntentResult {
  clientSecret: string
  paymentIntentId: string
}

export interface PaymentRepository {
  getById(id: string): Promise<Payment>
  getByOrderId(orderId: string): Promise<Payment>
  createIntent(amount: number, currency: string): Promise<CreateIntentResult>
}
