import type { AxiosInstance } from 'axios'
import type { UserCreationPort, CreateUserData } from '../../domain/ports/UserCreationPort'

export class HttpUserCreationAdapter implements UserCreationPort {
  constructor(private readonly client: AxiosInstance) {}

  async create(data: CreateUserData): Promise<void> {
    await this.client.post('/api/v1/users', data)
  }
}
