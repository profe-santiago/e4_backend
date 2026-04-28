import type { PaymentRepository } from '../../domain/ports/PaymentRepository'
import type { Payment } from '../../domain/entities/Payment'

export class GetPaymentByOrderUseCase {
  constructor(private readonly paymentRepository: PaymentRepository) {}

  execute(orderId: string): Promise<Payment> {
    return this.paymentRepository.getByOrderId(orderId)
  }
}
