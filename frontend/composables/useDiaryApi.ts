import type { CepAlert, DiaryEntry, DiaryEntryResponse, ProductDto, Recommendation } from '~/types/api'

export function useDiaryApi() {
  const api = useApi()
  return {
    list: () => api.get<DiaryEntry[]>('/api/diary'),
    addFlushing: (body: { occurredAt: string; durationMinutes: number; trigger: string | null }) =>
      api.post<DiaryEntryResponse>('/api/diary/flushing', body),
    addSun: (body: { occurredAt: string; uvIndex: number; minutesInSun: number; sunscreenSpf: number }) =>
      api.post<DiaryEntryResponse>('/api/diary/sun', body),
    addProduct: (body: { occurredAt: string; product: ProductDto }) =>
      api.post<DiaryEntryResponse>('/api/diary/products', body),
    addSymptom: (body: { occurredAt: string; type: string; location: string }) =>
      api.post<DiaryEntryResponse>('/api/diary/symptoms', body),
    alerts: () => api.get<CepAlert[]>('/api/cep/alerts'),
    markSeen: (id: number) => api.post<CepAlert>(`/api/cep/alerts/${id}/seen`),
    recommendations: () => api.get<Recommendation[]>('/api/cep/recommendations'),
    demoWeek: () => api.post<{ entries: number; note: string; alerts: CepAlert[] }>('/api/demo/cep-week'),
  }
}
