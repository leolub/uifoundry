import { useCallback, useEffect, useRef, useState } from 'react'
import { ArrowLeft, Clipboard, FileCode2, ImagePlus, LoaderCircle, Trash2, Upload, WandSparkles } from 'lucide-react'
import { ApiRequestError } from '../../services/api/client'
import { getProject, type Project } from '../../services/api/projects'
import { generateInterface, getLatestGeneration, type Generation } from '../../services/api/generations'
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
  if (error instanceof ApiRequestError) {
    const messages: Record<string, string> = {
      AI_PROVIDER_NOT_CONFIGURED: 'Gemini is not configured. Restart the backend with GEMINI_API_KEY set.',
      AI_PROVIDER_AUTHENTICATION_FAILED: 'Gemini rejected the configured API key. Check the key in the backend terminal.',
      AI_MODEL_NOT_AVAILABLE: 'The configured Gemini model is unavailable for this key or API version.',
      AI_PROVIDER_RATE_LIMITED: 'The Gemini free-tier quota or rate limit has been reached. Try again later.',
      AI_REQUEST_REJECTED: 'Gemini rejected the request format. Check the backend log for the sanitized provider detail.',
      AI_PROVIDER_RESPONSE_INVALID: 'Gemini responded, but its generated result was not valid structured code.',
      GENERATED_OUTPUT_INVALID: 'Gemini returned files that did not pass UIFoundry safety validation.',
    }
    if (error.code && messages[error.code]) return messages[error.code]
  }
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
  const [generation, setGeneration] = useState<Generation | null>(null)
  const [generationLoading, setGenerationLoading] = useState(true)
  const [generating, setGenerating] = useState(false)
  const [generationError, setGenerationError] = useState('')
  const [instruction, setInstruction] = useState('')
  const [selectedPath, setSelectedPath] = useState('')
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
    getLatestGeneration(projectId)
      .then((latest) => {
        setGeneration(latest)
        setSelectedPath(latest.files[0]?.path ?? '')
      })
      .catch((error) => {
        if (!(error instanceof ApiRequestError && error.status === 404)) {
          setGenerationError(errorMessage(error, 'Could not load the latest generation.'))
        }
      })
      .finally(() => setGenerationLoading(false))
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

  const generate = async () => {
    if (!source || generating) return
    setGenerating(true)
    setGenerationError('')
    try {
      const result = await generateInterface(projectId, instruction)
      setGeneration(result)
      setSelectedPath(result.files[0]?.path ?? '')
    } catch (error) {
      setGenerationError(errorMessage(error, 'Generation failed. Please try again.'))
    } finally {
      setGenerating(false)
    }
  }

  if (projectError) {
    return <main className="grid min-h-screen place-items-center bg-bg p-6 text-text"><div className="border border-border bg-surface-1 p-8"><h1 className="text-xl font-semibold">Workspace unavailable</h1><p className="mt-3 text-text-muted">{projectError}</p><a className="mt-6 inline-flex text-primary" href="/projects">Return to projects</a></div></main>
  }
  if (!project) return <main className="grid min-h-screen place-items-center bg-bg text-text-muted">Loading workspace...</main>

  const busy = action !== 'idle' && action !== 'loading'
  const selectedFile = generation?.files.find((file) => file.path === selectedPath) ?? generation?.files[0]

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

        <section className="flex min-h-[520px] min-w-0 flex-col bg-surface-1">
          <div className="flex items-center justify-between border-b border-border px-5 py-3 font-mono text-xs text-text-muted"><span>GENERATED CODE</span><span>{generation ? `${generation.provider} / ${generation.model}` : 'WAITING'}</span></div>
          <div className="border-b border-border bg-surface-2 p-5">
            <label className="text-xs font-semibold uppercase tracking-wider text-text-muted" htmlFor="generation-instruction">Optional instructions</label>
            <textarea id="generation-instruction" className="mt-2 min-h-24 w-full resize-y border border-border bg-bg p-3 text-sm text-text outline-none focus:border-primary disabled:opacity-50" maxLength={2000} disabled={generating} value={instruction} onChange={(event) => setInstruction(event.target.value)} placeholder="Keep the navigation dark and preserve the responsive layout." />
            <div className="mt-3 flex flex-wrap items-center justify-between gap-3"><span className="text-xs text-text-muted">{instruction.length}/2000 · One click sends one Gemini request</span><button className="inline-flex items-center gap-2 bg-primary px-4 py-2.5 text-sm font-semibold text-white hover:bg-primary-hover disabled:cursor-not-allowed disabled:opacity-50" type="button" disabled={!source || generating || action === 'loading'} onClick={() => void generate()}>{generating ? <LoaderCircle className="animate-spin" size={16} /> : <WandSparkles size={16} />}{generating ? 'Generating React interface...' : 'Generate Interface'}</button></div>
            <p className="mt-3 text-xs text-text-muted">Generation sends the source image and optional instruction to the configured Google Gemini service for processing.</p>
            {!source && action !== 'loading' && <p className="mt-2 text-xs text-text-muted">Upload a source screenshot to enable generation.</p>}
            {generationError && <div className="mt-4 border-l-4 border-primary bg-bg p-3 text-sm text-primary" role="alert">{generationError}</div>}
          </div>

          {generating ? (
            <div className="grid flex-1 place-items-center p-8 text-center"><div><LoaderCircle className="mx-auto animate-spin text-primary" size={34} /><p className="mt-4 font-medium">Analyzing screenshot...</p><p className="mt-2 text-sm text-text-muted">Gemini is generating a structured React interface. This request is sent only once.</p></div></div>
          ) : generationLoading ? (
            <div className="grid flex-1 place-items-center text-text-muted">Loading generated files...</div>
          ) : generation && selectedFile ? (
            <div className="flex min-h-0 flex-1 flex-col">
              <div className="border-b border-border p-4"><p className="text-sm">{generation.summary}</p><p className="mt-1 text-xs text-text-muted">Generated {new Date(generation.completedAt ?? generation.createdAt).toLocaleString()}</p></div>
              <div className="flex gap-1 overflow-x-auto border-b border-border bg-bg px-3 pt-3" role="tablist" aria-label="Generated files">{generation.files.map((file) => <button key={file.path} className={`shrink-0 border border-b-0 px-3 py-2 font-mono text-xs ${file.path === selectedFile.path ? 'border-border bg-surface-2 text-text' : 'border-transparent text-text-muted hover:text-text'}`} type="button" role="tab" aria-selected={file.path === selectedFile.path} onClick={() => setSelectedPath(file.path)}>{file.path.replace(/^src\//, '')}</button>)}</div>
              <pre className="min-h-0 flex-1 overflow-auto bg-surface-2 p-5 text-xs leading-6 text-text"><code>{selectedFile.content}</code></pre>
            </div>
          ) : (
            <div className="grid flex-1 place-items-center p-8 text-center text-text-muted"><div><FileCode2 className="mx-auto" size={34} /><p className="mt-4 text-text">Generated interface files will appear here</p><p className="mt-2 max-w-sm text-sm leading-6">Code is displayed as read-only text. Live execution and editing arrive in later phases.</p></div></div>
          )}
          <div className="border-t border-border p-4 font-mono text-xs text-text-muted">READ-ONLY SOURCE · MONACO AND SANDPACK ARE NOT ENABLED</div>
        </section>
      </div>
    </main>
  )
}
