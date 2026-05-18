import type { AxiosInstance } from 'axios'
import type { UserRepository } from '../../domain/ports/UserRepository'
import type { User, UpdateUserRequest, CreateProfileRequest } from '../../domain/entities/User'

export class HttpUserAdapter implements UserRepository {
  constructor(private readonly client: AxiosInstance) {}

  async createMe(data: CreateProfileRequest): Promise<User> {
    const { data: user } = await this.client.post<User>('/api/v1/users', data)
    return user
  }

  async getMe(): Promise<User> {
    const { data } = await this.client.get<User>('/api/v1/users/me')
    return data
  }

  async updateMe(request: UpdateUserRequest): Promise<User> {
    const { data } = await this.client.put<User>('/api/v1/users/me', request)
    return data
  }

  async deleteMe(): Promise<void> {
    await this.client.delete('/api/v1/users/me')
  }
}
