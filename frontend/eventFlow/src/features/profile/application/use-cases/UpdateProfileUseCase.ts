import type { UserRepository } from '../../domain/ports/UserRepository'
import type { User, UpdateUserRequest } from '../../domain/entities/User'

export class UpdateProfileUseCase {
  constructor(private readonly userRepository: UserRepository) {}

  execute(data: UpdateUserRequest): Promise<User> {
    return this.userRepository.updateMe(data)
  }
}
