import { FormEvent, useEffect, useState } from 'react'
import { FolderOpen, MoreHorizontal, Plus, Trash2 } from 'lucide-react'
import { useAuth } from '../../auth/useAuth'
import {
  createProject,
  deleteProject,
  listProjects,
  renameProject,
  type Project,
} from '../../services/api/projects'

export function DashboardPage() {
  const { currentUser } = useAuth()
  const [projects, setProjects] = useState<Project[]>([])
  const [isLoading, setIsLoading] = useState(true)
  const [error, setError] = useState('')
  const [isCreating, setIsCreating] = useState(false)
  const [newProjectName, setNewProjectName] = useState('')
  const [isSaving, setIsSaving] = useState(false)

  useEffect(() => {
    listProjects()
      .then(setProjects)
      .catch((requestError) => setError(
        requestError instanceof Error ? requestError.message : 'Could not load projects.',
      ))
      .finally(() => setIsLoading(false))
  }, [])

  async function handleCreate(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    if (!newProjectName.trim()) {
      setError('Enter a project name.')
      return
    }
    setError('')
    setIsSaving(true)
    try {
      const project = await createProject(newProjectName)
      setProjects((current) => [project, ...current])
      setNewProjectName('')
      setIsCreating(false)
    } catch (requestError) {
      setError(requestError instanceof Error ? requestError.message : 'Could not create project.')
    } finally {
      setIsSaving(false)
    }
  }

  async function handleRename(project: Project) {
    const name = window.prompt('Rename project', project.name)
    if (name === null || name.trim() === '' || name.trim() === project.name) return
    setError('')
    try {
      const updated = await renameProject(project.id, name)
      setProjects((current) => current.map((item) => item.id === updated.id ? updated : item))
    } catch (requestError) {
      setError(requestError instanceof Error ? requestError.message : 'Could not rename project.')
    }
  }

  async function handleDelete(project: Project) {
    if (!window.confirm(`Delete “${project.name}”? This cannot be undone.`)) return
    setError('')
    try {
      await deleteProject(project.id)
      setProjects((current) => current.filter((item) => item.id !== project.id))
    } catch (requestError) {
      setError(requestError instanceof Error ? requestError.message : 'Could not delete project.')
    }
  }

  return (
    <main className="min-h-screen bg-bg text-text">
      <header className="border-b border-border bg-surface-1">
        <div className="mx-auto flex max-w-7xl items-center justify-between px-6 py-4">
          <a className="flex items-center gap-3 font-semibold" href="/projects">
            <span className="grid size-9 place-items-center bg-primary text-sm font-black">UI</span>
            UIFoundry
          </a>
          <span className="text-sm text-text-muted">{currentUser?.email}</span>
        </div>
      </header>

      <section className="mx-auto max-w-7xl px-6 py-12">
        <div className="flex flex-wrap items-end justify-between gap-6 border-b border-border pb-8">
          <div>
            <p className="font-mono text-xs uppercase tracking-[0.22em] text-accent-cyan">Persistent workspace</p>
            <h1 className="mt-3 text-4xl font-black tracking-[-0.03em]">My Projects</h1>
          </div>
          <button
            className="inline-flex items-center gap-2 bg-primary px-5 py-3 font-semibold hover:bg-primary-hover"
            onClick={() => setIsCreating((value) => !value)}
            type="button"
          >
            <Plus size={18} /> New Project
          </button>
        </div>

        {isCreating && (
          <form className="mt-6 flex max-w-2xl gap-3 border border-border bg-surface-1 p-4" onSubmit={handleCreate}>
            <input
              autoFocus
              className="min-w-0 flex-1 border border-border bg-bg px-4 py-3 outline-none focus:border-primary"
              maxLength={120}
              onChange={(event) => setNewProjectName(event.target.value)}
              placeholder="Landing Page Recreation"
              value={newProjectName}
            />
            <button className="bg-primary px-5 font-semibold disabled:opacity-60" disabled={isSaving} type="submit">
              {isSaving ? 'Creating…' : 'Create'}
            </button>
          </form>
        )}

        {error && <p className="mt-6 border-l-4 border-primary bg-surface-1 px-4 py-3" role="alert">{error}</p>}

        {isLoading ? (
          <p className="mt-10 text-text-muted">Loading projects…</p>
        ) : projects.length === 0 ? (
          <div className="mt-10 border border-dashed border-border bg-surface-1 p-12 text-center">
            <FolderOpen className="mx-auto text-text-muted" size={32} />
            <h2 className="mt-5 text-xl font-semibold">No projects yet</h2>
            <p className="mt-2 text-text-muted">Create the first workspace for your next interface.</p>
          </div>
        ) : (
          <div className="mt-8 grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
            {projects.map((project) => (
              <article className="group border border-border bg-surface-1 p-5" key={project.id}>
                <div className="flex items-start justify-between gap-4">
                  <a className="min-w-0 flex-1" href={`/projects/${project.id}`}>
                    <FolderOpen className="text-primary" size={22} />
                    <h2 className="mt-8 truncate text-lg font-semibold">{project.name}</h2>
                    <p className="mt-2 text-xs text-text-muted">Updated {formatDate(project.updatedAt)}</p>
                  </a>
                  <MoreHorizontal className="text-text-muted" size={18} />
                </div>
                <div className="mt-5 flex gap-2 border-t border-border pt-4">
                  <button className="px-3 py-2 text-sm text-text-muted hover:bg-surface-2 hover:text-text" onClick={() => handleRename(project)} type="button">
                    Rename
                  </button>
                  <button className="inline-flex items-center gap-1 px-3 py-2 text-sm text-text-muted hover:bg-surface-2 hover:text-text" onClick={() => handleDelete(project)} type="button">
                    <Trash2 size={14} /> Delete
                  </button>
                </div>
              </article>
            ))}
          </div>
        )}
      </section>
    </main>
  )
}

function formatDate(value: string) {
  return new Intl.DateTimeFormat(undefined, { dateStyle: 'medium' }).format(new Date(value))
}
