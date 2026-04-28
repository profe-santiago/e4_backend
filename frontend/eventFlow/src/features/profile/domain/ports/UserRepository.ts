import type { User, UpdateUserRequest } from '../entities/User'

export interface UserRepository {
  getMe(): Promise<User>
  updateMe(data: UpdateUserRequest): Promise<User>
  deleteMe(): Promise<void>
}
