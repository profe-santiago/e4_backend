export interface TicketType {
  id: number
  eventId: string
  name: string
  price: number
  currency: string
  totalQuantity: number
  availableQuantity: number
  description: string | null
}

export interface CreateTicketTypeRequest {
  name: string
  description?: string
  price: number
  currency?: string
  totalQuantity: number
}

export interface UpdateTicketTypeRequest {
  name: string
  description?: string
  price: number
  currency?: string
  totalQuantity: number
}
