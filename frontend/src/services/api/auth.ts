import { apiRequest } from './client'

export type CurrentUser = {
  id: string
  email: string
  createdAt: string
}

export type RegisteredUser = CurrentUser

export type LoginResponse = {
  accessToken: string
  tokenType: 'Bearer'
  expiresIn: number
  user: CurrentUser
}

export async function registerUser(email: string, password: string): Promise<RegisteredUser> {
  return apiRequest<RegisteredUser>('/api/v1/auth/register', {
    method: 'POST',
    body: JSON.stringify({ email, password }),
  })
}

export async function loginUser(email: string, password: string): Promise<LoginResponse> {
  return apiRequest<LoginResponse>('/api/v1/auth/login', {
    method: 'POST',
    body: JSON.stringify({ email, password }),
  })
}

export async function getCurrentUser(): Promise<CurrentUser> {
  return apiRequest<CurrentUser>('/api/v1/auth/me')
}
