import type { User, UpdateUserRequest, CreateProfileRequest } from '../entities/User'

export interface UserRepository {
  createMe(data: CreateProfileRequest): Promise<User>
  getMe(): Promise<User>
  updateMe(data: UpdateUserRequest): Promise<User>
  deleteMe(): Promise<void>
}
