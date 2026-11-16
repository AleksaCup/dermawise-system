<template>
  <section>
    <div class="d-flex justify-space-between align-start flex-wrap ga-4 mb-6">
      <div>
        <h1 class="page-title">Korisnici</h1>
        <p class="page-lead mb-0">Nalozi korisnika, dermatologa i administratora. Deaktiviran nalog ne može da se prijavi.</p>
      </div>
      <v-btn color="primary" :prepend-icon="mdiPlus" @click="openDialog">Novi nalog</v-btn>
    </div>

    <v-alert v-if="error" type="error" class="mb-4" role="alert">{{ error }}</v-alert>

    <v-card>
      <div v-if="loading" class="pa-8 text-center">
        <v-progress-circular indeterminate color="primary" aria-label="Učitavanje" />
      </div>
      <div v-else class="table-wrap">
        <v-table>
          <thead>
            <tr>
              <th>Ime</th>
              <th>Uloga</th>
              <th>Napravljen</th>
              <th>Aktivan</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="u in users" :key="u.id">
              <td>
                <div class="font-weight-bold">{{ u.fullName }}</div>
                <div class="text-body-2 muted">{{ u.email }}</div>
              </td>
              <td>{{ ROLE_LABELS[u.role] }}</td>
              <td class="text-no-wrap">{{ formatDate(u.createdAt) }}</td>
              <td>
                <v-switch
                  :model-value="u.active"
                  :disabled="u.id === auth.user?.userId || busy === u.id"
                  :aria-label="`Aktivan nalog: ${u.fullName}`"
                  hide-details
                  @update:model-value="toggle(u, !!$event)"
                />
              </td>
            </tr>
          </tbody>
        </v-table>
      </div>
    </v-card>

    <v-dialog v-model="dialog" max-width="480">
      <v-card>
        <v-card-title>Novi nalog</v-card-title>
        <v-card-text>
          <v-form @submit.prevent="create">
            <v-text-field v-model="form.fullName" label="Ime i prezime" :error-messages="fieldError('fullName')" class="mb-2" />
            <v-text-field v-model="form.email" label="Email" type="email" :error-messages="fieldError('email')" class="mb-2" />
            <v-text-field
              v-model="form.password"
              label="Lozinka"
              type="password"
              autocomplete="new-password"
              :error-messages="fieldError('password')"
              class="mb-2"
            />
            <v-select v-model="form.role" :items="roleOptions" label="Uloga" />
            <v-alert v-if="formError" type="error" class="mb-2" role="alert">{{ formError }}</v-alert>
            <div class="d-flex justify-end ga-2">
              <v-btn variant="text" @click="dialog = false">Otkaži</v-btn>
              <v-btn type="submit" color="primary" :loading="saving">Napravi</v-btn>
            </div>
          </v-form>
        </v-card-text>
      </v-card>
    </v-dialog>
  </section>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { mdiPlus } from '@mdi/js'
import { ApiError } from '~/composables/useApi'
import { ROLE_LABELS, useAuthStore } from '~/stores/auth'
import type { Role, UserRow } from '~/types/api'

definePageMeta({ roles: ['ADMIN'] })

const adminApi = useAdminApi()
const auth = useAuthStore()
const roleOptions = (Object.keys(ROLE_LABELS) as Role[]).map((value) => ({ value, title: ROLE_LABELS[value] }))

const users = ref<UserRow[]>([])
const loading = ref(true)
const error = ref('')
const busy = ref<number | null>(null)

const dialog = ref(false)
const saving = ref(false)
const form = ref({ fullName: '', email: '', password: '', role: 'DERMATOLOGIST' as Role })
const formError = ref('')
const fieldErrors = ref<Record<string, string>>({})

function fieldError(name: string) {
  return fieldErrors.value[name] ? [fieldErrors.value[name]] : []
}

async function load() {
  try {
    users.value = await adminApi.users()
  } catch (e) {
    error.value = e instanceof ApiError ? e.message : 'Korisnici nisu učitani'
  } finally {
    loading.value = false
  }
}

onMounted(load)

async function toggle(user: UserRow, active: boolean) {
  busy.value = user.id
  error.value = ''
  try {
    const updated = await adminApi.setActive(user.id, active)
    users.value = users.value.map((u) => (u.id === updated.id ? updated : u))
  } catch (e) {
    error.value = e instanceof ApiError ? e.message : 'Izmena nije uspela'
  } finally {
    busy.value = null
  }
}

function openDialog() {
  form.value = { fullName: '', email: '', password: '', role: 'DERMATOLOGIST' }
  formError.value = ''
  fieldErrors.value = {}
  dialog.value = true
}

async function create() {
  saving.value = true
  formError.value = ''
  fieldErrors.value = {}
  try {
    await adminApi.createUser(form.value)
    dialog.value = false
    await load()
  } catch (e) {
    if (e instanceof ApiError) {
      formError.value = e.message
      fieldErrors.value = e.fieldErrors
    }
  } finally {
    saving.value = false
  }
}
</script>

<style scoped>
.table-wrap {
  overflow-x: auto;
}
</style>
