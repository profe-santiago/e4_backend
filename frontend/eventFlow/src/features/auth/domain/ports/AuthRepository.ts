import type { AuthResponse } from '../entities/AuthResponse'

export interface AuthRepository {
  login(email: string, password: string): Promise<AuthResponse>
  register(email: string, password: string): Promise<AuthResponse>
}
