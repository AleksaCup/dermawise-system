import type { ApiErrorBody } from '~/types/api'

export class ApiError extends Error {
  constructor(
    public status: number,
    message: string,
    public fieldErrors: Record<string, string> = {},
  ) {
    super(message)
  }
}

export const TOKEN_KEY = 'dermawise.token'

export function useApi() {
  const config = useRuntimeConfig()

  async function request<T>(path: string, options: { method?: string; body?: unknown; query?: Record<string, unknown> } = {}): Promise<T> {
    const token = localStorage.getItem(TOKEN_KEY)
    try {
      return await $fetch<T>(path, {
        baseURL: config.public.apiBase as string,
        method: (options.method ?? 'GET') as 'GET',
        body: options.body as Record<string, unknown> | undefined,
        query: options.query,
        headers: token ? { Authorization: `Bearer ${token}` } : {},
      })
    } catch (e: unknown) {
      const err = e as { status?: number; data?: Partial<ApiErrorBody> }
      if (!err.status) {
        throw new ApiError(0, 'Server nije dostupan. Proverite da li je backend pokrenut.')
      }
      throw new ApiError(err.status, err.data?.message ?? 'Došlo je do greške', err.data?.fieldErrors ?? {})
    }
  }

  return {
    get: <T>(path: string, query?: Record<string, unknown>) => request<T>(path, { query }),
    post: <T>(path: string, body?: unknown) => request<T>(path, { method: 'POST', body }),
    put: <T>(path: string, body?: unknown) => request<T>(path, { method: 'PUT', body }),
    patch: <T>(path: string, query?: Record<string, unknown>) => request<T>(path, { method: 'PATCH', query }),
    del: <T>(path: string) => request<T>(path, { method: 'DELETE' }),
  }
}
