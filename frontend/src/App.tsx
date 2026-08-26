import { ArrowRight, Code2, Frame, Image, PanelsTopLeft } from 'lucide-react'
import { RegisterPage } from './features/auth/RegisterPage'
import { LoginPage } from './features/auth/LoginPage'

const inputModes = [
  { label: 'Screenshot', description: 'Upload, drop, or paste an image.', icon: Image },
  { label: 'Website', description: 'Capture a public page from its URL.', icon: PanelsTopLeft },
  { label: 'Figma frame', description: 'Render a selected frame through Figma.', icon: Frame },
  { label: 'Wireframe', description: 'Reconstruct structure from a mockup.', icon: Code2 },
]

function App() {
  if (window.location.pathname === '/register') {
    return <RegisterPage />
  }
  if (window.location.pathname === '/login') {
    return <LoginPage />
  }

  return (
    <main className="min-h-screen bg-bg text-text">
      <nav className="border-b border-border bg-surface-1">
        <div className="mx-auto flex max-w-7xl items-center justify-between px-6 py-4">
          <a className="flex items-center gap-3 font-semibold tracking-tight" href="/">
            <span className="grid size-9 place-items-center bg-primary text-sm font-black">FF</span>
            UIFoundry
          </a>
          <a className="border border-border px-3 py-1.5 text-xs text-text-muted hover:text-text" href="/login">Sign in</a>
        </div>
      </nav>

      <section className="mx-auto grid max-w-7xl gap-12 px-6 py-20 lg:grid-cols-[1.15fr_0.85fr] lg:items-center">
        <div>
          <p className="mb-5 font-mono text-xs uppercase tracking-[0.24em] text-accent-cyan">
            Visual input → runnable interface
          </p>
          <h1 className="max-w-3xl text-5xl font-black leading-[0.98] tracking-[-0.04em] sm:text-7xl">
            Turn a design into code you can own.
          </h1>
          <p className="mt-7 max-w-2xl text-lg leading-8 text-text-muted">
            UIFoundry converts visual references into constrained React, TypeScript,
            and Tailwind files, then keeps them editable, previewable, and versioned.
          </p>
          <div className="mt-9 flex flex-wrap items-center gap-4">
            <a className="inline-flex items-center gap-2 bg-primary px-5 py-3 font-semibold transition-colors hover:bg-primary-hover" href="/register">
              Create an account <ArrowRight size={18} />
            </a>
            <span className="text-sm text-text-muted">3 free generations · temporary for 24 hours</span>
          </div>
        </div>

        <div className="border border-border bg-surface-1 p-3">
          <div className="flex items-center justify-between border-b border-border px-3 py-2 font-mono text-xs text-text-muted">
            <span>INPUT PIPELINE</span>
            <span className="text-success">BASE UI READY</span>
          </div>
          <div className="grid gap-px bg-border sm:grid-cols-2">
            {inputModes.map(({ label, description, icon: Icon }, index) => (
              <article className="relative bg-surface-2 p-6" key={label}>
                {index === 0 && <span className="absolute inset-y-0 left-0 w-1 bg-primary" />}
                <Icon className="mb-8 text-text" size={22} />
                <h2 className="font-semibold">{label}</h2>
                <p className="mt-2 text-sm leading-6 text-text-muted">{description}</p>
              </article>
            ))}
          </div>
        </div>
      </section>

      <section className="border-y border-border bg-surface-1">
        <div className="mx-auto grid max-w-7xl divide-y divide-border px-6 md:grid-cols-3 md:divide-x md:divide-y-0">
          {[
            ['01', 'Normalize', 'Every supported source becomes one safe design image.'],
            ['02', 'Generate', 'Gemini returns a validated, dependency-controlled file set.'],
            ['03', 'Refine', 'Edit, preview, checkpoint, restore, and export your result.'],
          ].map(([number, title, body]) => (
            <article className="py-8 md:px-8 md:first:pl-0" key={number}>
              <span className="font-mono text-xs text-primary">{number}</span>
              <h2 className="mt-4 text-xl font-semibold">{title}</h2>
              <p className="mt-2 text-sm leading-6 text-text-muted">{body}</p>
            </article>
          ))}
        </div>
      </section>
    </main>
  )
}

export default App
