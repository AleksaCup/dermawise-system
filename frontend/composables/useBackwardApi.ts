import type { AllergenCheck, ContactCause, HypothesisResponse } from '~/types/api'

export function useBackwardApi() {
  const api = useApi()
  return {
    hypothesis: (goal: string, patientId?: number) =>
      api.get<HypothesisResponse>(`/api/backward/hypotheses/${goal}`, patientId ? { patientId } : undefined),
    contactCause: (patientId?: number) =>
      api.get<ContactCause[]>('/api/backward/contact-dermatitis-cause', patientId ? { patientId } : undefined),
    allergenCheck: (sastojak: string, patientId?: number) =>
      api.get<AllergenCheck>('/api/backward/allergen-check', patientId ? { sastojak, patientId } : { sastojak }),
  }
}
