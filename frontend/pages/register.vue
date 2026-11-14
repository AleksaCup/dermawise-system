<template>
  <div class="auth-wrap">
    <h1 class="page-title">Registracija</h1>
    <p class="page-lead">Nalog je potreban da bi se sačuvali upitnik, analize i dnevnik kože.</p>

    <v-card>
      <v-card-text>
        <v-form @submit.prevent="submit">
          <v-text-field
            v-model="fullName"
            label="Ime i prezime"
            autocomplete="name"
            :error-messages="fieldError('fullName')"
            class="mb-2"
          />
          <v-text-field
            v-model="email"
            label="Email"
            type="email"
            autocomplete="email"
            :error-messages="fieldError('email')"
            class="mb-2"
          />
          <v-text-field
            v-model="password"
            label="Lozinka"
            :type="showPassword ? 'text' : 'password'"
            autocomplete="new-password"
            hint="Najmanje 6 znakova"
            persistent-hint
            :append-inner-icon="showPassword ? mdiEyeOff : mdiEye"
            :error-messages="fieldError('password')"
            class="mb-4"
            @click:append-inner="showPassword = !showPassword"
          />
          <v-alert v-if="error" type="error" class="mb-4" role="alert">{{ error }}</v-alert>
          <v-btn type="submit" color="primary" block size="large" :loading="loading">Napravi nalog</v-btn>
        </v-form>
      </v-card-text>
    </v-card>

    <p class="mt-4 muted">
      Već imate nalog? <NuxtLink to="/login">Prijavite se</NuxtLink>
    </p>
  </div>
</template>

<script setup lang="ts">
import { mdiEye, mdiEyeOff } from '@mdi/js'
import { ApiError } from '~/composables/useApi'
import { useAuthStore } from '~/stores/auth'

definePageMeta({ public: true })

const auth = useAuthStore()

const fullName = ref('')
const email = ref('')
const password = ref('')
const showPassword = ref(false)
const loading = ref(false)
const error = ref('')
const fieldErrors = ref<Record<string, string>>({})

function fieldError(name: string) {
  return fieldErrors.value[name] ? [fieldErrors.value[name]] : []
}

async function submit() {
  loading.value = true
  error.value = ''
  fieldErrors.value = {}
  try {
    await auth.register(fullName.value, email.value, password.value)
    await navigateTo(auth.homePath())
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
