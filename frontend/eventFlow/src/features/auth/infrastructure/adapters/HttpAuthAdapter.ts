import type { AxiosInstance } from 'axios'
import type { AuthRepository } from '../../domain/ports/AuthRepository'
import type { AuthResponse } from '../../domain/entities/AuthResponse'
import { decodeJwtRole } from '../../domain/entities/AuthResponse'

interface AuthApiResponse {
  userId: string
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
    return { ...data, role: decodeJwtRole(data.token) }
  }

  async register(email: string, password: string): Promise<AuthResponse> {
    const { data } = await this.client.post<AuthApiResponse>('/api/v1/auth/register', {
      email,
      password,
    })
    return { ...data, role: decodeJwtRole(data.token) }
  }
}
