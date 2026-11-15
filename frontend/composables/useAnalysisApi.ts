import type { AnalysisResponse } from '~/types/api'

export function useAnalysisApi() {
  const api = useApi()
  return {
    run: () => api.post<AnalysisResponse>('/api/analysis'),
    latest: () => api.get<AnalysisResponse>('/api/analysis/latest'),
  }
}
