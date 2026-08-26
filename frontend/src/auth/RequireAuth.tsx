import { ReactNode, useEffect } from 'react'
import { useAuth } from './useAuth'

export function RequireAuth({ children }: { children: ReactNode }) {
  const { isAuthenticated, isInitializing } = useAuth()

  useEffect(() => {
    if (!isInitializing && !isAuthenticated) {
      window.location.replace('/login')
    }
  }, [isAuthenticated, isInitializing])

  if (isInitializing || !isAuthenticated) {
    return (
      <main className="grid min-h-screen place-items-center bg-bg text-text-muted">
        <p className="border border-border bg-surface-1 px-6 py-4">Checking workspace access…</p>
      </main>
    )
  }

  return children
}
