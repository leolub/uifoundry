export type RegisteredUser = {
  id: string
  email: string
  createdAt: string
}

type ApiError = {
  code?: string
  message?: string
  details?: Record<string, string>
}

const apiBaseUrl = (import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080').replace(/\/$/, '')

export async function registerUser(email: string, password: string): Promise<RegisteredUser> {
  const response = await fetch(`${apiBaseUrl}/api/v1/auth/register`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ email, password }),
  })

  if (!response.ok) {
    const error = (await response.json().catch(() => null)) as ApiError | null
    const fieldMessage = error?.details && Object.values(error.details)[0]
    throw new Error(fieldMessage ?? error?.message ?? 'Registration failed. Please try again.')
  }

  return response.json() as Promise<RegisteredUser>
}
