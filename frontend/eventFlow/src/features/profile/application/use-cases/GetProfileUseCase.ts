import type { UserRepository } from '../../domain/ports/UserRepository'
import type { User } from '../../domain/entities/User'

export class GetProfileUseCase {
  constructor(private readonly userRepository: UserRepository) {}

  execute(): Promise<User> {
    return this.userRepository.getMe()
  }
}
