import { apiRequest } from './client'

export type GeneratedFile = { path: string; content: string }

export type Generation = {
  id: string
  status: 'RUNNING' | 'SUCCEEDED' | 'FAILED'
  provider: string
  model: string
  summary: string
  createdAt: string
  completedAt: string | null
  files: GeneratedFile[]
}

const generationsPath = (projectId: string) => `/api/v1/projects/${projectId}/generations`

export function generateInterface(projectId: string, instruction: string) {
  return apiRequest<Generation>(generationsPath(projectId), {
    method: 'POST',
    body: JSON.stringify({ instruction: instruction.trim() || null }),
  })
}

export function getLatestGeneration(projectId: string) {
  return apiRequest<Generation>(`${generationsPath(projectId)}/latest`)
}
