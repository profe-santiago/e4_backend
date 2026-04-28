import type { AxiosInstance } from 'axios'
import type { TicketTypeRepository } from '../../domain/ports/TicketTypeRepository'
import type { TicketType, CreateTicketTypeRequest, UpdateTicketTypeRequest } from '../../domain/entities/TicketType'

export class HttpTicketTypeAdapter implements TicketTypeRepository {
  constructor(private readonly client: AxiosInstance) {}

  async listByEvent(eventId: string): Promise<TicketType[]> {
    const { data } = await this.client.get<TicketType[]>(`/api/v1/events/${eventId}/ticket-types`)
    return data
  }

  async create(eventId: string, request: CreateTicketTypeRequest): Promise<TicketType> {
    const { data } = await this.client.post<TicketType>(
      `/api/v1/events/${eventId}/ticket-types`,
      request,
    )
    return data
  }

  async update(eventId: string, id: number, request: UpdateTicketTypeRequest): Promise<TicketType> {
    const { data } = await this.client.put<TicketType>(
      `/api/v1/events/${eventId}/ticket-types/${id}`,
      request,
    )
    return data
  }

  async delete(eventId: string, id: number): Promise<void> {
    await this.client.delete(`/api/v1/events/${eventId}/ticket-types/${id}`)
  }
}
