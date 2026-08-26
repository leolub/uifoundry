import { useEffect, useState } from 'react'
import { ArrowLeft, Code2, History, Image, MessageSquareText, Monitor } from 'lucide-react'
import { getProject, type Project } from '../../services/api/projects'

type WorkspacePageProps = {
  projectId: string
}

const railItems = [
  { label: 'Input', icon: Image },
  { label: 'Files', icon: Code2 },
  { label: 'AI Refine', icon: MessageSquareText },
  { label: 'Versions', icon: History },
]

export function WorkspacePage({ projectId }: WorkspacePageProps) {
  const [project, setProject] = useState<Project | null>(null)
  const [error, setError] = useState('')

  useEffect(() => {
    getProject(projectId)
      .then(setProject)
      .catch((requestError) => setError(
        requestError instanceof Error ? requestError.message : 'Could not open project.',
      ))
  }, [projectId])

  if (error) {
    return (
      <main className="grid min-h-screen place-items-center bg-bg p-6 text-text">
        <div className="border border-border bg-surface-1 p-8">
          <h1 className="text-xl font-semibold">Workspace unavailable</h1>
          <p className="mt-3 text-text-muted">{error}</p>
          <a className="mt-6 inline-flex text-primary" href="/projects">Return to projects</a>
        </div>
      </main>
    )
  }

  if (!project) {
    return <main className="grid min-h-screen place-items-center bg-bg text-text-muted">Loading workspace…</main>
  }

  return (
    <main className="flex min-h-screen flex-col bg-bg text-text">
      <header className="flex flex-wrap items-center justify-between gap-4 border-b border-border bg-surface-1 px-5 py-3">
        <div className="flex min-w-0 items-center gap-4">
          <a className="text-text-muted hover:text-text" href="/projects" aria-label="Back to projects"><ArrowLeft size={18} /></a>
          <div className="min-w-0">
            <h1 className="truncate font-semibold">{project.name}</h1>
            <p className="font-mono text-xs text-text-muted">WORKSPACE FOUNDATION</p>
          </div>
        </div>
        <span className="border border-border px-3 py-1.5 text-xs text-text-muted">Saved</span>
      </header>

      <div className="grid flex-1 lg:grid-cols-[220px_1fr_1fr]">
        <aside className="border-b border-border bg-surface-1 p-3 lg:border-b-0 lg:border-r">
          <nav className="grid grid-cols-2 gap-1 lg:grid-cols-1">
            {railItems.map(({ label, icon: Icon }, index) => (
              <button className={`flex items-center gap-3 px-3 py-3 text-left text-sm ${index === 0 ? 'border-l-4 border-primary bg-surface-2 text-text' : 'text-text-muted'}`} key={label} type="button">
                <Icon size={16} /> {label}
              </button>
            ))}
          </nav>
          <div className="mt-6 border border-dashed border-border p-4 text-xs leading-5 text-text-muted">
            Design input controls arrive in the image-upload feature.
          </div>
        </aside>

        <section className="flex min-h-[360px] flex-col border-b border-border bg-surface-2 lg:border-b-0 lg:border-r">
          <div className="flex items-center justify-between border-b border-border px-4 py-3 font-mono text-xs text-text-muted">
            <span>CODE EDITOR</span><span>PLACEHOLDER</span>
          </div>
          <div className="grid flex-1 place-items-center p-8 text-center text-text-muted">
            <div><Code2 className="mx-auto" size={30} /><p className="mt-4">Generated files will be editable here.</p></div>
          </div>
        </section>

        <section className="flex min-h-[360px] flex-col bg-surface-1">
          <div className="flex items-center justify-between border-b border-border px-4 py-3 font-mono text-xs text-text-muted">
            <span>LIVE PREVIEW</span><span>DESKTOP</span>
          </div>
          <div className="grid flex-1 place-items-center p-8 text-center text-text-muted">
            <div><Monitor className="mx-auto" size={30} /><p className="mt-4">Runnable preview will appear after generation.</p></div>
          </div>
        </section>
      </div>
    </main>
  )
}
