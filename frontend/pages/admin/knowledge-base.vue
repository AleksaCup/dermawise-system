<template>
  <section>
    <h1 class="page-title">Baza znanja</h1>
    <p class="page-lead">
      Pravila koja su trenutno učitana. Posle svake izmene template-a baza se ponovo gradi i dobija novu verziju.
    </p>

    <v-alert v-if="error" type="error" class="mb-4" role="alert">{{ error }}</v-alert>

    <div v-if="loading" class="py-12 text-center">
      <v-progress-circular indeterminate color="primary" aria-label="Učitavanje" />
    </div>

    <template v-else-if="info">
      <v-row>
        <v-col v-for="stat in stats" :key="stat.label" cols="6" md="3">
          <v-card class="h-100">
            <v-card-text>
              <div class="text-body-2 muted">{{ stat.label }}</div>
              <div class="stat tabular">{{ stat.value }}</div>
            </v-card-text>
          </v-card>
        </v-col>
      </v-row>

      <v-tabs v-model="baseName" color="primary" class="mt-8 mb-4">
        <v-tab v-for="b in info.bases" :key="b.name" :value="b.name">{{ BASE_LABELS[b.name] ?? b.name }}</v-tab>
      </v-tabs>

      <template v-if="base">
        <p class="muted">
          {{ base.rules }} pravila i {{ base.queries }} upita.
          <span v-for="(count, group) in base.rulesPerAgendaGroup" :key="group" class="mr-3">
            {{ PHASE_LABELS[group] ?? group }}: <span class="tabular">{{ count }}</span>
          </span>
        </p>

        <v-row class="mb-2">
          <v-col cols="12" md="8">
            <v-text-field v-model="search" label="Pretraga po nazivu, opisu ili izvoru" hide-details clearable />
          </v-col>
          <v-col cols="12" md="4">
            <v-select v-model="phase" :items="phaseOptions" label="Faza" hide-details clearable />
          </v-col>
        </v-row>

        <v-card>
          <div class="table-wrap">
            <v-table density="compact">
              <thead>
                <tr>
                  <th>Pravilo</th>
                  <th>Faza</th>
                  <th>Opis</th>
                  <th>Izvor</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="r in filteredRules" :key="r.name">
                  <td class="font-weight-bold">{{ r.name }}</td>
                  <td class="text-no-wrap">{{ PHASE_LABELS[r.agendaGroup] ?? r.agendaGroup }}</td>
                  <td>{{ r.opis }}</td>
                  <td class="muted">{{ r.source }}</td>
                </tr>
              </tbody>
            </v-table>
          </div>
          <p v-if="!filteredRules.length" class="pa-4 muted mb-0">Nijedno pravilo ne odgovara pretrazi.</p>
        </v-card>

        <h2 class="section-title">Upiti (backward chaining)</h2>
        <v-chip v-for="q in base.queryList" :key="q" class="mr-2 mb-2">{{ q }}</v-chip>
      </template>
    </template>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { ApiError } from '~/composables/useApi'
import type { KnowledgeBaseInfo } from '~/types/api'

definePageMeta({ roles: ['ADMIN'] })

const BASE_LABELS: Record<string, string> = {
  analysis: 'Analiza',
  backward: 'Hipoteze',
  cep: 'Dnevnik (CEP)',
}

const adminApi = useAdminApi()
const info = ref<KnowledgeBaseInfo | null>(null)
const loading = ref(true)
const error = ref('')
const baseName = ref('analysis')
const search = ref<string | null>('')
const phase = ref<string | null>(null)

onMounted(async () => {
  try {
    info.value = await adminApi.knowledgeBase()
  } catch (e) {
    error.value = e instanceof ApiError ? e.message : 'Baza znanja nije učitana'
  } finally {
    loading.value = false
  }
})

const stats = computed(() => {
  if (!info.value) {
    return []
  }
  return [
    { label: 'Verzija baze', value: info.value.version },
    { label: 'Poslednja izgradnja', value: formatDate(info.value.rebuiltAt) },
    { label: 'Redova template-a', value: info.value.templateRows },
    { label: 'Otvorenih CEP sesija', value: info.value.openCepSessions },
  ]
})

const base = computed(() => info.value?.bases.find((b) => b.name === baseName.value))

const phaseOptions = computed(() =>
  Object.keys(base.value?.rulesPerAgendaGroup ?? {}).map((g) => ({ value: g, title: PHASE_LABELS[g] ?? g })),
)

const filteredRules = computed(() => {
  const term = (search.value ?? '').trim().toLowerCase()
  return (base.value?.ruleList ?? []).filter((r) => {
    if (phase.value && r.agendaGroup !== phase.value) {
      return false
    }
    if (!term) {
      return true
    }
    return [r.name, r.opis, r.source].some((text) => text?.toLowerCase().includes(term))
  })
})
</script>

<style scoped>
.stat {
  font-size: 1.4rem;
  font-weight: 700;
  margin-top: 4px;
}

.table-wrap {
  overflow-x: auto;
}
</style>
