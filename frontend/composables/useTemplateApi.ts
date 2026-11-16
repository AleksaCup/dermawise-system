import type { TemplateRule, TemplateRuleRequest } from '~/types/api'

export function useTemplateApi() {
  const api = useApi()
  return {
    list: () => api.get<TemplateRule[]>('/api/templates'),
    create: (row: TemplateRuleRequest) => api.post<TemplateRule>('/api/templates', row),
    update: (id: number, row: TemplateRuleRequest) => api.put<TemplateRule>(`/api/templates/${id}`, row),
    remove: (id: number) => api.del<void>(`/api/templates/${id}`),
    generatedDrl: () => api.get<string>('/api/templates/generated-drl'),
  }
}
