import { FormEvent, useState } from 'react'
import { ArrowLeft, Check, Hammer } from 'lucide-react'
import { registerUser, type RegisteredUser } from '../../services/api/auth'

export function RegisterPage() {
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [registeredUser, setRegisteredUser] = useState<RegisteredUser | null>(null)

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setError('')

    if (!email.trim()) {
      setError('Enter your email address.')
      return
    }
    if (password.length < 8 || password.length > 72) {
      setError('Password must be between 8 and 72 characters.')
      return
    }

    setIsSubmitting(true)
    try {
      setRegisteredUser(await registerUser(email, password))
    } catch (registrationError) {
      setError(registrationError instanceof Error ? registrationError.message : 'Registration failed.')
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
          <p className="font-mono text-xs uppercase tracking-[0.22em] text-accent-cyan">Persistent workspace</p>
          <h1 className="mt-5 max-w-lg text-5xl font-black leading-none tracking-[-0.04em]">
            Keep every interface you forge.
          </h1>
          <p className="mt-6 max-w-md leading-7 text-text-muted">
            Create an account for durable projects and version history. Registration never blocks the guest demo.
          </p>
        </div>
        <p className="font-mono text-xs text-text-muted">REACT · SPRING BOOT · POSTGRESQL</p>
      </section>

      <section className="flex items-center justify-center px-6 py-12">
        <div className="w-full max-w-md">
          <a className="mb-10 inline-flex items-center gap-2 text-sm text-text-muted hover:text-text" href="/">
            <ArrowLeft size={16} /> Back to UIFoundry
          </a>

          {registeredUser ? (
            <div className="border border-border bg-surface-1 p-8" role="status">
              <span className="grid size-11 place-items-center bg-success text-black"><Check size={22} /></span>
              <h1 className="mt-7 text-3xl font-bold tracking-tight">Account created.</h1>
              <p className="mt-3 leading-7 text-text-muted">
                <strong className="font-medium text-text">{registeredUser.email}</strong> is ready. Login will be connected in the next authentication feature.
              </p>
              <a className="mt-8 inline-flex bg-primary px-5 py-3 font-semibold hover:bg-primary-hover" href="/">
                Return home
              </a>
            </div>
          ) : (
            <>
              <Hammer className="mb-6 text-primary" size={30} />
              <h1 className="text-4xl font-black tracking-[-0.03em]">Create your account</h1>
              <p className="mt-3 text-text-muted">Use email and a password of 8–72 characters.</p>

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
                    autoComplete="new-password"
                    className="w-full border border-border bg-surface-1 px-4 py-3 text-text outline-none focus:border-primary"
                    minLength={8}
                    maxLength={72}
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
                  {isSubmitting ? 'Creating account…' : 'Create account'}
                </button>
              </form>
            </>
          )}
        </div>
      </section>
    </main>
  )
}
