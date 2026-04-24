export interface TicketType {
  id: number
  eventId: string
  name: string
  price: number
  quantity: number
  availableQuantity: number
  description: string | null
}
