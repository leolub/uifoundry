type ApiErrorBody = {
  code?: string
  message?: string
  details?: Record<string, string>
}

const apiBaseUrl = (import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080').replace(/\/$/, '')
const accessTokenKey = 'uifoundry.accessToken'

export class ApiRequestError extends Error {
  constructor(
    message: string,
    public readonly status: number,
    public readonly code?: string,
  ) {
    super(message)
  }
}

export const tokenStorage = {
  get: () => sessionStorage.getItem(accessTokenKey),
  set: (token: string) => sessionStorage.setItem(accessTokenKey, token),
  clear: () => sessionStorage.removeItem(accessTokenKey),
}

export async function apiRequest<T>(path: string, init: RequestInit = {}): Promise<T> {
  const headers = new Headers(init.headers)
  if (init.body && !headers.has('Content-Type')) {
    headers.set('Content-Type', 'application/json')
  }

  const accessToken = tokenStorage.get()
  if (accessToken) {
    headers.set('Authorization', `Bearer ${accessToken}`)
  }

  const response = await fetch(`${apiBaseUrl}${path}`, { ...init, headers })
  if (!response.ok) {
    const error = (await response.json().catch(() => null)) as ApiErrorBody | null
    const fieldMessage = error?.details && Object.values(error.details)[0]
    throw new ApiRequestError(
      fieldMessage ?? error?.message ?? 'The request failed. Please try again.',
      response.status,
      error?.code,
    )
  }

  return response.json() as Promise<T>
}
