export interface ApiErrorBody {
  timestamp: string
  status: number
  error: string
  message: string
  fieldErrors: Record<string, string>
}

export type Role = 'USER' | 'DERMATOLOGIST' | 'ADMIN'

export interface AuthResponse {
  token: string
  userId: number
  email: string
  fullName: string
  role: Role
}
