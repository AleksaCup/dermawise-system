import type { Role } from '~/types/api'

declare module '#app' {
  interface PageMeta {
    roles?: Role[]
    public?: boolean
  }
}

export {}
