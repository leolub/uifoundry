import { apiRequest } from './client'

export type Project = {
  id: string
  name: string
  createdAt: string
  updatedAt: string
}

export function listProjects(): Promise<Project[]> {
  return apiRequest<Project[]>('/api/v1/projects')
}

export function getProject(projectId: string): Promise<Project> {
  return apiRequest<Project>(`/api/v1/projects/${projectId}`)
}

export function createProject(name: string): Promise<Project> {
  return apiRequest<Project>('/api/v1/projects', {
    method: 'POST',
    body: JSON.stringify({ name }),
  })
}

export function renameProject(projectId: string, name: string): Promise<Project> {
  return apiRequest<Project>(`/api/v1/projects/${projectId}`, {
    method: 'PATCH',
    body: JSON.stringify({ name }),
  })
}

export function deleteProject(projectId: string): Promise<void> {
  return apiRequest<void>(`/api/v1/projects/${projectId}`, { method: 'DELETE' })
}
