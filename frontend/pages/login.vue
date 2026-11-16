<template>
  <div class="auth-wrap">
    <h1 class="page-title">Prijava</h1>
    <p class="page-lead">Prijavite se da biste videli svoj profil kože, analizu i dnevnik.</p>

    <v-card>
      <v-card-text>
        <v-form @submit.prevent="submit">
          <v-text-field
            v-model="email"
            label="Email"
            type="email"
            autocomplete="username"
            :error-messages="fieldError('email')"
            class="mb-2"
          />
          <v-text-field
            v-model="password"
            label="Lozinka"
            :type="showPassword ? 'text' : 'password'"
            autocomplete="current-password"
            :error-messages="fieldError('password')"
          >
            <template #append-inner>
              <v-btn
                :icon="showPassword ? mdiEyeOff : mdiEye"
                :aria-label="showPassword ? 'Sakrij lozinku' : 'Prikaži lozinku'"
                variant="text"
                size="small"
                density="comfortable"
                @click="showPassword = !showPassword"
              />
            </template>
          </v-text-field>
          <v-alert v-if="error" type="error" class="mb-4" role="alert">{{ error }}</v-alert>
          <v-btn type="submit" color="primary" block size="large" :loading="loading">Prijavi se</v-btn>
        </v-form>
      </v-card-text>
    </v-card>

    <p class="mt-4 muted">
      Nemate nalog? <NuxtLink to="/register">Registrujte se</NuxtLink>
    </p>

    <v-card class="mt-6">
      <v-card-title>Demo nalozi</v-card-title>
      <v-card-text>
        <v-table density="compact">
          <tbody>
            <tr v-for="account in demoAccounts" :key="account.email">
              <td>{{ account.email }}</td>
              <td class="muted">{{ account.password }}</td>
              <td class="muted">{{ account.note }}</td>
              <td class="text-right">
                <v-btn variant="text" size="small" @click="fill(account)">Popuni</v-btn>
              </td>
            </tr>
          </tbody>
        </v-table>
      </v-card-text>
    </v-card>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { mdiEye, mdiEyeOff } from '@mdi/js'
import { ApiError } from '~/composables/useApi'
import { useAuthStore } from '~/stores/auth'

definePageMeta({ public: true })

const auth = useAuthStore()
const route = useRoute()

const email = ref('')
const password = ref('')
const showPassword = ref(false)
const loading = ref(false)
const error = ref('')
const fieldErrors = ref<Record<string, string>>({})

const demoAccounts = [
  { email: 'ana@dermawise.rs', password: 'user123', note: 'korisnik, umerene akne' },
  { email: 'milica@dermawise.rs', password: 'user123', note: 'korisnica, trudna' },
  { email: 'dermatolog@dermawise.rs', password: 'derm123', note: 'dermatolog' },
  { email: 'admin@dermawise.rs', password: 'admin123', note: 'administrator' },
]

function fill(account: { email: string; password: string }) {
  email.value = account.email
  password.value = account.password
}

function fieldError(name: string) {
  return fieldErrors.value[name] ? [fieldErrors.value[name]] : []
}

async function submit() {
  loading.value = true
  error.value = ''
  fieldErrors.value = {}
  try {
    await auth.login(email.value, password.value)
    const next = typeof route.query.next === 'string' ? route.query.next : auth.homePath()
    await navigateTo(next)
  } catch (e) {
    if (e instanceof ApiError) {
      error.value = e.message
      fieldErrors.value = e.fieldErrors
    }
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.auth-wrap {
  max-width: 520px;
  margin: 0 auto;
}
</style>
