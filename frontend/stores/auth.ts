import { defineStore } from 'pinia'
import type { AuthResponse, Role } from '~/types/api'
import { TOKEN_KEY } from '~/composables/useApi'

const USER_KEY = 'dermawise.user'

interface SessionUser {
  userId: number
  email: string
  fullName: string
  role: Role
}

export const ROLE_LABELS: Record<Role, string> = {
  USER: 'Korisnik',
  DERMATOLOGIST: 'Dermatolog',
  ADMIN: 'Administrator',
}

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: null as string | null,
    user: null as SessionUser | null,
  }),
  getters: {
    isLoggedIn: (state) => state.token !== null,
    role: (state) => state.user?.role ?? null,
  },
  actions: {
    restore() {
      const token = localStorage.getItem(TOKEN_KEY)
      const user = localStorage.getItem(USER_KEY)
      if (token && user) {
        this.token = token
        this.user = JSON.parse(user)
      }
    },
    async login(email: string, password: string) {
      const response = await useApi().post<AuthResponse>('/api/auth/login', { email, password })
      this.save(response)
    },
    async register(fullName: string, email: string, password: string) {
      const response = await useApi().post<AuthResponse>('/api/auth/register', { fullName, email, password })
      this.save(response)
    },
    logout() {
      this.token = null
      this.user = null
      localStorage.removeItem(TOKEN_KEY)
      localStorage.removeItem(USER_KEY)
    },
    save(response: AuthResponse) {
      this.token = response.token
      this.user = {
        userId: response.userId,
        email: response.email,
        fullName: response.fullName,
        role: response.role,
      }
      localStorage.setItem(TOKEN_KEY, response.token)
      localStorage.setItem(USER_KEY, JSON.stringify(this.user))
    },
    homePath(): string {
      if (this.role === 'USER') {
        return '/results'
      }
      if (this.role === 'DERMATOLOGIST') {
        return '/dermatologist/patients'
      }
      return '/'
    },
  },
})
