import type { KnowledgeBaseInfo, Role, UserRow } from '~/types/api'

export function useAdminApi() {
  const api = useApi()
  return {
    users: () => api.get<UserRow[]>('/api/admin/users'),
    createUser: (body: { fullName: string; email: string; password: string; role: Role }) =>
      api.post<UserRow>('/api/admin/users', body),
    setActive: (id: number, active: boolean) => api.patch<UserRow>(`/api/admin/users/${id}/active`, { active }),
    knowledgeBase: () => api.get<KnowledgeBaseInfo>('/api/admin/knowledge-base'),
  }
}
