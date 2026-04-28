import type { UserRepository } from '../../domain/ports/UserRepository'

export class DeleteAccountUseCase {
  constructor(private readonly userRepository: UserRepository) {}

  execute(): Promise<void> {
    return this.userRepository.deleteMe()
  }
}
