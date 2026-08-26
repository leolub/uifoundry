import { apiFetch, apiRequest } from './client'

export type ProjectSource = {
  id: string
  projectId: string
  sourceType: 'IMAGE_UPLOAD'
  originalFilename: string
  contentType: string
  sizeBytes: number
  createdAt: string
  updatedAt: string
}

const sourcePath = (projectId: string) => `/api/v1/projects/${projectId}/source-image`

export function getProjectSource(projectId: string) {
  return apiRequest<ProjectSource>(sourcePath(projectId))
}

export function uploadProjectSource(projectId: string, file: File) {
  const body = new FormData()
  body.append('file', file)
  return apiRequest<ProjectSource>(sourcePath(projectId), { method: 'PUT', body })
}

export async function fetchProjectSourceBlob(projectId: string) {
  const response = await apiFetch(`${sourcePath(projectId)}/content`)
  return response.blob()
}

export function deleteProjectSource(projectId: string) {
  return apiRequest<void>(sourcePath(projectId), { method: 'DELETE' })
}
