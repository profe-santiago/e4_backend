import type { AxiosInstance } from 'axios'
import type { AuthRepository } from '../../domain/ports/AuthRepository'
import type { AuthResponse } from '../../domain/entities/AuthResponse'
import { decodeJwtPayload } from '../../domain/entities/AuthResponse'

interface AuthApiResponse {
  email: string
  token: string
}

export class HttpAuthAdapter implements AuthRepository {
  constructor(private readonly client: AxiosInstance) {}

  async login(email: string, password: string): Promise<AuthResponse> {
    const { data } = await this.client.post<AuthApiResponse>('/api/v1/auth/login', {
      email,
      password,
    })
    const { role, userId } = decodeJwtPayload(data.token)
    return { ...data, userId, role }
  }

  async register(email: string, password: string): Promise<AuthResponse> {
    const { data } = await this.client.post<AuthApiResponse>('/api/v1/auth/register', {
      email,
      password,
    })
    const { role, userId } = decodeJwtPayload(data.token)
    return { ...data, userId, role }
  }
}
