import type { ProfileResponse, Questionnaire } from '~/types/api'

export function useProfileApi() {
  const api = useApi()
  return {
    get: () => api.get<ProfileResponse>('/api/profile'),
    saveQuestionnaire: (questionnaire: Questionnaire) =>
      api.put<ProfileResponse>('/api/profile/questionnaire', questionnaire),
  }
}
