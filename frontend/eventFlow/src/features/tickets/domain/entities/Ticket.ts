export type TicketStatus = 'ACTIVE' | 'USED' | 'CANCELLED' | 'EXPIRED'

export interface Ticket {
  id: string
  orderItemId: string
  orderId: string
  userId: string
  eventId: string
  ticketTypeId: number
  qrCode: string
  status: TicketStatus
  purchasedAt: string
  usedAt: string | null
}
