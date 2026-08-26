import { useCallback, useEffect, useRef, useState } from 'react'
import { ArrowLeft, Clipboard, ImagePlus, Monitor, Trash2, Upload } from 'lucide-react'
import { ApiRequestError } from '../../services/api/client'
import { getProject, type Project } from '../../services/api/projects'
import {
  deleteProjectSource,
  fetchProjectSourceBlob,
  getProjectSource,
  uploadProjectSource,
  type ProjectSource,
} from '../../services/api/projectSources'

type WorkspacePageProps = { projectId: string }
type SourceAction = 'idle' | 'loading' | 'uploading' | 'replacing' | 'deleting'

const allowedTypes = ['image/png', 'image/jpeg', 'image/webp']
const maxUploadBytes = 10 * 1024 * 1024

function errorMessage(error: unknown, fallback: string) {
  if (error instanceof ApiRequestError && error.status === 401) return 'Your session expired. Sign in again.'
  return error instanceof Error ? error.message : fallback
}

function formatBytes(bytes: number) {
  if (bytes < 1024) return `${bytes} B`
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`
  return `${(bytes / 1024 / 1024).toFixed(1)} MB`
}

export function WorkspacePage({ projectId }: WorkspacePageProps) {
  const [project, setProject] = useState<Project | null>(null)
  const [projectError, setProjectError] = useState('')
  const [source, setSource] = useState<ProjectSource | null>(null)
  const [previewUrl, setPreviewUrl] = useState('')
  const [sourceError, setSourceError] = useState('')
  const [action, setAction] = useState<SourceAction>('loading')
  const [dragActive, setDragActive] = useState(false)
  const fileInputRef = useRef<HTMLInputElement>(null)
  const previewUrlRef = useRef('')

  const replacePreviewUrl = useCallback((nextUrl: string) => {
    if (previewUrlRef.current) URL.revokeObjectURL(previewUrlRef.current)
    previewUrlRef.current = nextUrl
    setPreviewUrl(nextUrl)
  }, [])

  const loadSource = useCallback(async () => {
    setAction('loading')
    setSourceError('')
    try {
      const metadata = await getProjectSource(projectId)
      const blob = await fetchProjectSourceBlob(projectId)
      setSource(metadata)
      replacePreviewUrl(URL.createObjectURL(blob))
    } catch (error) {
      if (error instanceof ApiRequestError && error.status === 404) {
        setSource(null)
        replacePreviewUrl('')
      } else {
        setSourceError(errorMessage(error, 'Could not load the source image.'))
      }
    } finally {
      setAction('idle')
    }
  }, [projectId, replacePreviewUrl])

  useEffect(() => {
    getProject(projectId).then(setProject).catch((error) => setProjectError(errorMessage(error, 'Could not open project.')))
    void loadSource()
  }, [loadSource, projectId])

  useEffect(() => () => {
    if (previewUrlRef.current) URL.revokeObjectURL(previewUrlRef.current)
  }, [])

  const submitFile = useCallback(async (file: File) => {
    setSourceError('')
    if (!allowedTypes.includes(file.type)) {
      setSourceError('Only PNG, JPEG, and WebP images are supported.')
      return
    }
    if (file.size > maxUploadBytes) {
      setSourceError('The image must be 10 MB or smaller.')
      return
    }
    setAction(source ? 'replacing' : 'uploading')
    try {
      const metadata = await uploadProjectSource(projectId, file)
      const blob = await fetchProjectSourceBlob(projectId)
      setSource(metadata)
      replacePreviewUrl(URL.createObjectURL(blob))
    } catch (error) {
      setSourceError(errorMessage(error, 'Upload failed. Please try again.'))
    } finally {
      setAction('idle')
      if (fileInputRef.current) fileInputRef.current.value = ''
    }
  }, [projectId, replacePreviewUrl, source])

  useEffect(() => {
    const pasteImage = (event: ClipboardEvent) => {
      const image = Array.from(event.clipboardData?.files ?? []).find((file) => file.type.startsWith('image/'))
      if (image) {
        event.preventDefault()
        void submitFile(image)
      }
    }
    window.addEventListener('paste', pasteImage)
    return () => window.removeEventListener('paste', pasteImage)
  }, [submitFile])

  const removeSource = async () => {
    setAction('deleting')
    setSourceError('')
    try {
      await deleteProjectSource(projectId)
      setSource(null)
      replacePreviewUrl('')
    } catch (error) {
      setSourceError(errorMessage(error, 'Could not remove the source image.'))
    } finally {
      setAction('idle')
    }
  }

  if (projectError) {
    return <main className="grid min-h-screen place-items-center bg-bg p-6 text-text"><div className="border border-border bg-surface-1 p-8"><h1 className="text-xl font-semibold">Workspace unavailable</h1><p className="mt-3 text-text-muted">{projectError}</p><a className="mt-6 inline-flex text-primary" href="/projects">Return to projects</a></div></main>
  }
  if (!project) return <main className="grid min-h-screen place-items-center bg-bg text-text-muted">Loading workspace...</main>

  const busy = action !== 'idle' && action !== 'loading'

  return (
    <main className="flex min-h-screen flex-col bg-bg text-text">
      <header className="flex flex-wrap items-center justify-between gap-4 border-b border-border bg-surface-1 px-5 py-3">
        <div className="flex min-w-0 items-center gap-4"><a className="text-text-muted hover:text-text" href="/projects" aria-label="Back to projects"><ArrowLeft size={18} /></a><div className="min-w-0"><h1 className="truncate font-semibold">{project.name}</h1><p className="font-mono text-xs text-text-muted">IMAGE SOURCE WORKSPACE</p></div></div>
        <span className="border border-border px-3 py-1.5 text-xs text-text-muted">{busy ? 'Saving source...' : 'Saved'}</span>
      </header>

      <div className="grid flex-1 lg:grid-cols-2">
        <section className="flex min-h-[520px] flex-col border-b border-border bg-surface-2 lg:border-b-0 lg:border-r">
          <div className="flex items-center justify-between border-b border-border px-5 py-3 font-mono text-xs text-text-muted"><span>SOURCE / INPUT</span><span>IMAGE_UPLOAD</span></div>
          <div className="flex flex-1 flex-col p-5">
            {action === 'loading' ? <div className="grid flex-1 place-items-center text-text-muted">Loading source...</div> : source && previewUrl ? (
              <div className="flex flex-1 flex-col">
                <div className="grid min-h-[320px] flex-1 place-items-center overflow-hidden border border-border bg-bg p-3"><img className="max-h-[60vh] max-w-full object-contain" src={previewUrl} alt={`Source screenshot ${source.originalFilename}`} /></div>
                <div className="mt-4 flex flex-wrap items-center justify-between gap-4 border border-border bg-surface-1 p-4"><div className="min-w-0"><p className="truncate text-sm font-medium">{source.originalFilename}</p><p className="mt-1 text-xs text-text-muted">{source.contentType} · {formatBytes(source.sizeBytes)}</p></div><div className="flex gap-2"><button className="inline-flex items-center gap-2 border border-border px-3 py-2 text-sm hover:bg-surface-2 disabled:opacity-50" type="button" disabled={busy} onClick={() => fileInputRef.current?.click()}><Upload size={15} /> Replace</button><button className="inline-flex items-center gap-2 border border-primary px-3 py-2 text-sm text-primary disabled:opacity-50" type="button" disabled={busy} onClick={() => void removeSource()}><Trash2 size={15} /> Remove</button></div></div>
              </div>
            ) : (
              <div
                className={`grid flex-1 place-items-center border border-dashed p-8 text-center ${dragActive ? 'border-primary bg-surface-1' : 'border-border bg-bg'}`}
                onDragEnter={(event) => { event.preventDefault(); setDragActive(true) }}
                onDragOver={(event) => event.preventDefault()}
                onDragLeave={(event) => { event.preventDefault(); setDragActive(false) }}
                onDrop={(event) => { event.preventDefault(); setDragActive(false); const file = event.dataTransfer.files[0]; if (file) void submitFile(file) }}
              ><div><ImagePlus className="mx-auto text-primary" size={38} /><h2 className="mt-5 text-lg font-semibold">Add a source screenshot</h2><p className="mt-2 max-w-md text-sm leading-6 text-text-muted">Drop an image here, paste a screenshot with Ctrl + V, or choose a PNG, JPEG, or WebP file up to 10 MB.</p><button className="mt-6 inline-flex items-center gap-2 bg-primary px-4 py-2.5 text-sm font-semibold text-white hover:bg-primary-hover disabled:opacity-50" type="button" disabled={busy} onClick={() => fileInputRef.current?.click()}><Upload size={16} /> Upload Screenshot</button><p className="mt-4 inline-flex items-center gap-2 text-xs text-text-muted"><Clipboard size={13} /> Clipboard paste is enabled in this workspace</p></div></div>
            )}
            <input ref={fileInputRef} className="hidden" type="file" accept="image/png,image/jpeg,image/webp" onChange={(event) => { const file = event.target.files?.[0]; if (file) void submitFile(file) }} />
            {sourceError && <div className="mt-4 border-l-4 border-primary bg-surface-1 p-3 text-sm text-primary" role="alert">{sourceError}</div>}
          </div>
        </section>

        <section className="flex min-h-[520px] flex-col bg-surface-1">
          <div className="flex items-center justify-between border-b border-border px-5 py-3 font-mono text-xs text-text-muted"><span>OUTPUT / PREVIEW</span><span>NOT GENERATED</span></div>
          <div className="grid flex-1 place-items-center p-8 text-center text-text-muted"><div><Monitor className="mx-auto" size={34} /><p className="mt-4 text-text">Generated interface will appear here</p><p className="mt-2 max-w-sm text-sm leading-6">The saved source image will become the visual input for multimodal generation in the next phase.</p></div></div>
          <div className="border-t border-border p-4 font-mono text-xs text-text-muted">FUTURE CODE / CONTROLS AREA — AI, MONACO, AND SANDPACK ARE NOT ENABLED</div>
        </section>
      </div>
    </main>
  )
}
