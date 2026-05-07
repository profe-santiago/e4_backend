export type OrderStatus = 'PENDING' | 'CONFIRMED' | 'FAILED' | 'CANCELLED' | 'REFUNDED'

export interface OrderItem {
  eventId: string
  ticketTypeId: number
  quantity: number
  unitPrice: number
}

export interface Order {
  id: string
  userId: string
  status: OrderStatus
  totalAmount: number
  paymentIntentId: string
  items: OrderItem[]
  createdAt: string
  updatedAt: string
}

export interface CreateOrderRequest {
  items: Array<{
    eventId: string
    ticketTypeId: number
    quantity: number
  }>
  paymentIntentId: string
}
