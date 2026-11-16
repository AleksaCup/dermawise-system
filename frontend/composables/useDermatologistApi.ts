import type {
  AnalysisResponse,
  CepAlert,
  PatientSummary,
  PendingRecommendation,
  ProfileResponse,
  Recommendation,
} from '~/types/api'

export function useDermatologistApi() {
  const api = useApi()
  const base = '/api/dermatologist'
  return {
    patients: () => api.get<PatientSummary[]>(`${base}/patients`),
    profile: (id: number) => api.get<ProfileResponse>(`${base}/patients/${id}/profile`),
    analysis: (id: number) => api.get<AnalysisResponse>(`${base}/patients/${id}/analysis`),
    alerts: (id: number) => api.get<CepAlert[]>(`${base}/patients/${id}/alerts`),
    pending: () => api.get<PendingRecommendation[]>(`${base}/recommendations/pending`),
    approve: (id: number, comment: string) =>
      api.post<Recommendation>(`${base}/recommendations/${id}/approve`, { comment: comment || null }),
    reject: (id: number, comment: string) =>
      api.post<Recommendation>(`${base}/recommendations/${id}/reject`, { comment }),
  }
}
