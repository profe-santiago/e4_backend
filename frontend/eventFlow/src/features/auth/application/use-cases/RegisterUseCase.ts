import type { AuthRepository } from '../../domain/ports/AuthRepository'
import type { UserCreationPort, CreateUserData } from '../../domain/ports/UserCreationPort'
import type { AuthResponse } from '../../domain/entities/AuthResponse'

interface RegisterData {
  email: string
  password: string
  firstName: string
  lastName: string
}

export class RegisterUseCase {
  constructor(
    private readonly authRepository: AuthRepository,
    private readonly userCreationPort: UserCreationPort
  ) {}

  async execute(data: RegisterData): Promise<AuthResponse> {
    const authResponse = await this.authRepository.register(data.email, data.password)

    const profileData: CreateUserData = {
      firstName: data.firstName,
      lastName: data.lastName,
      email: data.email,
    }

    await this.userCreationPort.create(profileData)

    return authResponse
  }
}
