import type { AxiosInstance } from 'axios'
import type { UserCreationPort, CreateUserData } from '../../domain/ports/UserCreationPort'

export class HttpUserCreationAdapter implements UserCreationPort {
  constructor(private readonly client: AxiosInstance) {}

  async create(data: CreateUserData, token: string): Promise<void> {
    await this.client.post('/api/v1/users', data, {
      headers: { Authorization: `Bearer ${token}` },
    })
  }
}
