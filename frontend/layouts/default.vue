<template>
  <v-app>
    <v-app-bar density="comfortable" color="surface">
      <v-container class="d-flex align-center py-0 ga-2" style="max-width: 1100px">
        <v-app-bar-nav-icon
          v-if="auth.isLoggedIn && links.length && mobile"
          aria-label="Otvori meni"
          @click="drawer = !drawer"
        />
        <NuxtLink to="/" class="brand">DermaWise</NuxtLink>

        <nav v-if="auth.isLoggedIn && !mobile" class="d-flex ga-1 ml-6" aria-label="Glavna navigacija">
          <v-btn v-for="link in links" :key="link.to" :to="link.to" variant="text" size="small">
            {{ link.label }}
          </v-btn>
        </nav>

        <v-spacer />

        <template v-if="auth.isLoggedIn && auth.user">
          <span v-if="!mobile" class="user-label">
            {{ auth.user.fullName }}
            <span class="muted">· {{ ROLE_LABELS[auth.user.role] }}</span>
          </span>
          <v-btn variant="outlined" size="small" @click="logout">Odjava</v-btn>
        </template>
        <v-btn v-else to="/login" variant="outlined" size="small">Prijava</v-btn>
      </v-container>
    </v-app-bar>

    <v-navigation-drawer v-if="auth.isLoggedIn && mobile" v-model="drawer" temporary>
      <v-list nav>
        <v-list-item v-if="auth.user" :title="auth.user.fullName" :subtitle="ROLE_LABELS[auth.user.role]" />
        <v-divider class="my-2" />
        <v-list-item v-for="link in links" :key="link.to" :to="link.to" :title="link.label" />
      </v-list>
    </v-navigation-drawer>

    <v-main>
      <v-container class="py-8" style="max-width: 1100px">
        <slot />
      </v-container>
    </v-main>
  </v-app>
</template>

<script setup lang="ts">
import { useDisplay } from 'vuetify'
import { ROLE_LABELS, useAuthStore } from '~/stores/auth'
import type { Role } from '~/types/api'

const auth = useAuthStore()
const { smAndDown: mobile } = useDisplay()
const drawer = ref(false)

const NAVIGATION: Record<Role, { to: string; label: string }[]> = {
  USER: [{ to: '/questionnaire', label: 'Upitnik' }],
  DERMATOLOGIST: [],
  ADMIN: [],
}

const links = computed(() => (auth.role ? NAVIGATION[auth.role] : []))

async function logout() {
  auth.logout()
  await navigateTo('/login')
}
</script>

<style scoped>
.brand {
  font-weight: 700;
  font-size: 1.15rem;
  color: rgb(var(--v-theme-on-surface));
  text-decoration: none;
}

.user-label {
  font-size: 0.9rem;
  margin-right: 8px;
}
</style>
