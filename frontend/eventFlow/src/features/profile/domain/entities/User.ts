export interface User {
  id: string
  firstName: string
  lastName: string
  email: string
  phone: string | null
  birthDate: string | null
  avatarUrl: string | null
  createdAt: string
  updatedAt: string
}

export interface UpdateUserRequest {
  firstName?: string
  lastName?: string
  phone?: string
  birthDate?: string
  avatarUrl?: string
}
