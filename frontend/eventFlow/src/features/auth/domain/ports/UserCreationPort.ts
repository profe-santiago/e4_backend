export interface CreateUserData {
  firstName: string
  lastName: string
  email: string
}

export interface UserCreationPort {
  create(data: CreateUserData): Promise<void>
}
