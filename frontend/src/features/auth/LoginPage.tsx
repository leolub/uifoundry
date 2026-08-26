import { FormEvent, useState } from 'react'
import { ArrowLeft, Check, KeyRound } from 'lucide-react'
import { useAuth } from '../../auth/useAuth'

export function LoginPage() {
  const { currentUser, isAuthenticated, isInitializing, login } = useAuth()
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')
  const [isSubmitting, setIsSubmitting] = useState(false)

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setError('')
    if (!email.trim() || !password) {
      setError('Enter your email and password.')
      return
    }

    setIsSubmitting(true)
    try {
      await login(email, password)
    } catch (loginError) {
      setError(loginError instanceof Error ? loginError.message : 'Login failed.')
    } finally {
      setIsSubmitting(false)
    }
  }

  return (
    <main className="grid min-h-screen bg-bg text-text lg:grid-cols-[0.8fr_1.2fr]">
      <section className="hidden border-r border-border bg-surface-1 p-12 lg:flex lg:flex-col lg:justify-between">
        <a className="flex items-center gap-3 font-semibold" href="/">
          <span className="grid size-9 place-items-center bg-primary text-sm font-black">UI</span>
          UIFoundry
        </a>
        <div>
          <p className="font-mono text-xs uppercase tracking-[0.22em] text-accent-cyan">Secure workspace access</p>
          <h1 className="mt-5 max-w-lg text-5xl font-black leading-none tracking-[-0.04em]">
            Return to your foundry.
          </h1>
          <p className="mt-6 max-w-md leading-7 text-text-muted">
            Sign in to verify your identity and continue with persistent projects in this browser session.
          </p>
        </div>
        <p className="font-mono text-xs text-text-muted">JWT ACCESS TOKEN · STATELESS API</p>
      </section>

      <section className="flex items-center justify-center px-6 py-12">
        <div className="w-full max-w-md">
          <a className="mb-10 inline-flex items-center gap-2 text-sm text-text-muted hover:text-text" href="/">
            <ArrowLeft size={16} /> Back to UIFoundry
          </a>

          {isInitializing ? (
            <p className="border border-border bg-surface-1 p-6 text-text-muted">Checking your session…</p>
          ) : isAuthenticated && currentUser ? (
            <div className="border border-border bg-surface-1 p-8" role="status">
              <span className="grid size-11 place-items-center bg-success text-black"><Check size={22} /></span>
              <h1 className="mt-7 text-3xl font-bold tracking-tight">Authenticated.</h1>
              <p className="mt-3 leading-7 text-text-muted">
                The access token was verified through <code className="text-text">/api/v1/auth/me</code> for{' '}
                <strong className="font-medium text-text">{currentUser.email}</strong>.
              </p>
              <a className="mt-8 inline-flex bg-primary px-5 py-3 font-semibold hover:bg-primary-hover" href="/projects">
                Open dashboard
              </a>
            </div>
          ) : (
            <>
              <KeyRound className="mb-6 text-primary" size={30} />
              <h1 className="text-4xl font-black tracking-[-0.03em]">Sign in</h1>
              <p className="mt-3 text-text-muted">Use the account you created in UIFoundry.</p>

              <form className="mt-9 space-y-6" onSubmit={handleSubmit} noValidate>
                <label className="block">
                  <span className="mb-2 block text-sm font-medium">Email</span>
                  <input
                    autoComplete="email"
                    className="w-full border border-border bg-surface-1 px-4 py-3 text-text outline-none focus:border-primary"
                    onChange={(event) => setEmail(event.target.value)}
                    placeholder="you@example.com"
                    required
                    type="email"
                    value={email}
                  />
                </label>
                <label className="block">
                  <span className="mb-2 block text-sm font-medium">Password</span>
                  <input
                    autoComplete="current-password"
                    className="w-full border border-border bg-surface-1 px-4 py-3 text-text outline-none focus:border-primary"
                    onChange={(event) => setPassword(event.target.value)}
                    required
                    type="password"
                    value={password}
                  />
                </label>

                {error && (
                  <p className="border-l-4 border-primary bg-surface-2 px-4 py-3 text-sm" role="alert">{error}</p>
                )}

                <button
                  className="w-full bg-primary px-5 py-3 font-semibold hover:bg-primary-hover disabled:cursor-not-allowed disabled:opacity-60"
                  disabled={isSubmitting}
                  type="submit"
                >
                  {isSubmitting ? 'Signing in…' : 'Sign in'}
                </button>
              </form>

              <p className="mt-6 text-sm text-text-muted">
                Need an account? <a className="text-text underline underline-offset-4" href="/register">Register</a>
              </p>
            </>
          )}
        </div>
      </section>
    </main>
  )
}
