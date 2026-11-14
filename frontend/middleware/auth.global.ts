import { useAuthStore } from '~/stores/auth'

export default defineNuxtRouteMiddleware((to) => {
  const auth = useAuthStore()
  if (!auth.isLoggedIn) {
    auth.restore()
  }

  if (to.meta.public) {
    if (auth.isLoggedIn && (to.path === '/login' || to.path === '/register')) {
      return navigateTo(auth.homePath())
    }
    return
  }

  if (!auth.isLoggedIn) {
    return navigateTo({ path: '/login', query: { next: to.fullPath } })
  }

  const roles = to.meta.roles
  if (roles && auth.role && !roles.includes(auth.role)) {
    return navigateTo(auth.homePath())
  }
})
