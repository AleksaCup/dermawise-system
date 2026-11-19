import type { IngredientOption } from '~/types/api'

export function useIngredients() {
  const options = useState<IngredientOption[]>('ingredients', () => [])
  const api = useApi()

  async function load() {
    if (!options.value.length) {
      try {
        options.value = await api.get<IngredientOption[]>('/api/backward/ingredients')
      } catch {
        options.value = []
      }
    }
  }

  return { options, load }
}
