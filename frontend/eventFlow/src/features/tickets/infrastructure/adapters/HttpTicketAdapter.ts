import type { AxiosInstance } from 'axios'
import type { TicketRepository } from '../../domain/ports/TicketRepository'
import type { Ticket } from '../../domain/entities/Ticket'
import type { PaginatedResponse } from '@/shared/types/pagination.types'

export class HttpTicketAdapter implements TicketRepository {
  constructor(private readonly client: AxiosInstance) {}

  async getMyTickets(page: number, size: number): Promise<PaginatedResponse<Ticket>> {
    const { data } = await this.client.get<PaginatedResponse<Ticket>>('/api/v1/tickets/my', {
      params: { page, size },
    })
    return data
  }

  async getById(id: string): Promise<Ticket> {
    const { data } = await this.client.get<Ticket>(`/api/v1/tickets/${id}`)
    return data
  }

  async validate(qrCode: string): Promise<Ticket> {
    const { data } = await this.client.post<Ticket>('/api/v1/tickets/validate', { qrCode })
    return data
  }
}
