<template>
  <section>
    <h1 class="page-title">Pacijenti</h1>
    <p class="page-lead">Korisnici sistema sa fototipom, poslednjom analizom i brojem preporuka koje čekaju potvrdu.</p>

    <v-alert v-if="error" type="error" class="mb-6" role="alert">{{ error }}</v-alert>

    <v-card>
      <div v-if="loading" class="pa-8 text-center">
        <v-progress-circular indeterminate color="primary" aria-label="Učitavanje" />
      </div>
      <p v-else-if="!patients.length" class="pa-4 muted mb-0">Nema korisnika.</p>
      <div v-else class="table-wrap">
        <v-table hover>
          <thead>
            <tr>
              <th>Ime</th>
              <th>Fototip</th>
              <th>Napomena</th>
              <th>Poslednja analiza</th>
              <th class="text-right">Na čekanju</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="p in patients" :key="p.id" class="clickable" @click="open(p.id)">
              <td>
                <NuxtLink :to="`/dermatologist/patients/${p.id}`" class="font-weight-bold" @click.stop>
                  {{ p.fullName }}
                </NuxtLink>
                <div class="text-body-2 muted">{{ p.email }}</div>
              </td>
              <td class="tabular">{{ p.fototip ? roman(p.fototip) : '-' }}</td>
              <td>
                <v-chip v-if="p.pregnant" color="warning" label>Trudnoća</v-chip>
                <span v-else-if="!p.questionnaireFilled" class="muted">Bez upitnika</span>
              </td>
              <td class="text-no-wrap">{{ p.lastAnalysisAt ? formatDate(p.lastAnalysisAt) : '-' }}</td>
              <td class="text-right tabular">{{ p.pendingReviews }}</td>
            </tr>
          </tbody>
        </v-table>
      </div>
    </v-card>
  </section>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { ApiError } from '~/composables/useApi'
import type { PatientSummary } from '~/types/api'

definePageMeta({ roles: ['DERMATOLOGIST'] })

const dermatologistApi = useDermatologistApi()
const patients = ref<PatientSummary[]>([])
const loading = ref(true)
const error = ref('')

onMounted(async () => {
  try {
    patients.value = await dermatologistApi.patients()
  } catch (e) {
    error.value = e instanceof ApiError ? e.message : 'Pacijenti nisu učitani'
  } finally {
    loading.value = false
  }
})

function open(id: number) {
  navigateTo(`/dermatologist/patients/${id}`)
}
</script>

<style scoped>
.clickable {
  cursor: pointer;
}

.table-wrap {
  overflow-x: auto;
}
</style>
