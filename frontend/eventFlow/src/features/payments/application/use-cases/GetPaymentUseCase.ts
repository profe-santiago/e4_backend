import type { PaymentRepository } from '../../domain/ports/PaymentRepository'
import type { Payment } from '../../domain/entities/Payment'

export class GetPaymentUseCase {
  constructor(private readonly paymentRepository: PaymentRepository) {}

  execute(id: string): Promise<Payment> {
    return this.paymentRepository.getById(id)
  }
}
