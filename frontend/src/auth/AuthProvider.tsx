import { ReactNode, useCallback, useEffect, useMemo, useState } from 'react'
import { AuthContext, type AuthContextValue } from './authContext'
import { getCurrentUser, loginUser, type CurrentUser } from '../services/api/auth'
import { tokenStorage } from '../services/api/client'

export function AuthProvider({ children }: { children: ReactNode }) {
  const [isInitializing, setIsInitializing] = useState(true)
  const [currentUser, setCurrentUser] = useState<CurrentUser | null>(null)
  const [accessToken, setAccessToken] = useState<string | null>(() => tokenStorage.get())

  useEffect(() => {
    if (!tokenStorage.get()) {
      setIsInitializing(false)
      return
    }

    getCurrentUser()
      .then(setCurrentUser)
      .catch(() => {
        tokenStorage.clear()
        setAccessToken(null)
        setCurrentUser(null)
      })
      .finally(() => setIsInitializing(false))
  }, [])

  const login = useCallback(async (email: string, password: string) => {
    const response = await loginUser(email, password)
    tokenStorage.set(response.accessToken)
    setAccessToken(response.accessToken)

    try {
      const verifiedUser = await getCurrentUser()
      setCurrentUser(verifiedUser)
      return verifiedUser
    } catch (error) {
      tokenStorage.clear()
      setAccessToken(null)
      setCurrentUser(null)
      throw error
    }
  }, [])

  const value = useMemo<AuthContextValue>(() => ({
    isInitializing,
    isAuthenticated: currentUser !== null && accessToken !== null,
    currentUser,
    accessToken,
    login,
  }), [accessToken, currentUser, isInitializing, login])

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}
