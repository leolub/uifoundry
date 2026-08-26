import { createContext } from 'react'
import type { CurrentUser } from '../services/api/auth'

export type AuthContextValue = {
  isInitializing: boolean
  isAuthenticated: boolean
  currentUser: CurrentUser | null
  accessToken: string | null
  login: (email: string, password: string) => Promise<CurrentUser>
}

export const AuthContext = createContext<AuthContextValue | null>(null)
