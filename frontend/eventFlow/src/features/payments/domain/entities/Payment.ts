export type PaymentStatus = 'PENDING' | 'APPROVED' | 'REJECTED' | 'REFUNDED'

export interface Payment {
  id: string
  orderId: string
  userId: string
  amount: number
  currency: string
  status: PaymentStatus
  paymentIntentId: string | null
  transactionId: string | null
  createdAt: string
  updatedAt: string | null
}
