import type { AuthRepository } from '../../domain/ports/AuthRepository'
import type { AuthResponse } from '../../domain/entities/AuthResponse'

export class LoginUseCase {
  constructor(private readonly authRepository: AuthRepository) {}

  execute(email: string, password: string): Promise<AuthResponse> {
    return this.authRepository.login(email, password)
  }
}
