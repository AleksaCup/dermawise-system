import type { ProfileResponse, Questionnaire, QuestionnaireExample } from '~/types/api'

export function useProfileApi() {
  const api = useApi()
  return {
    get: () => api.find<ProfileResponse>('/api/profile'),
    examples: () => api.get<QuestionnaireExample[]>('/api/profile/examples'),
    saveQuestionnaire: (questionnaire: Questionnaire) =>
      api.put<ProfileResponse>('/api/profile/questionnaire', questionnaire),
  }
}
