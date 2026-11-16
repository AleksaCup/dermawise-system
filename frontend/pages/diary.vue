<template>
  <section>
    <h1 class="page-title">Dnevnik kože</h1>
    <p class="page-lead">
      Beležite flushing, sunce, nove proizvode i simptome. Sistem prati unose kroz vreme i javlja kada
      primeti obrazac.
    </p>

    <v-alert v-if="error" type="error" class="mb-6" role="alert">{{ error }}</v-alert>

    <v-row>
      <v-col cols="12" md="7">
        <v-card>
          <v-tabs v-model="tab" color="primary" grow>
            <v-tab value="FLUSHING">Flushing</v-tab>
            <v-tab value="SUN">Sunce</v-tab>
            <v-tab value="NEW_PRODUCT">Novi proizvod</v-tab>
            <v-tab value="SYMPTOM">Simptom</v-tab>
          </v-tabs>
          <v-divider />
          <v-card-text class="pa-6">
            <EntryForm :key="tab" :type="tab" @saved="onSaved" />
            <div v-if="lastAlerts !== null" class="mt-4" role="status" aria-live="polite">
              <p v-if="!lastAlerts.length" class="muted mb-0">Unos je sačuvan. Nema novih upozorenja.</p>
              <v-alert v-for="a in lastAlerts" :key="a.id" type="warning" class="mt-2">
                <strong>{{ ALERT_LABELS[a.type] ?? a.type }}.</strong> {{ a.message }}
              </v-alert>
            </div>
          </v-card-text>
        </v-card>
      </v-col>

      <v-col cols="12" md="5">
        <v-card>
          <v-card-title class="d-flex justify-space-between align-center">
            <span>Upozorenja</span>
            <span class="text-body-2 muted">{{ unseen }} novih</span>
          </v-card-title>
          <v-card-text>
            <p v-if="!alerts.length" class="muted mb-0">Još nema upozorenja.</p>
            <div v-for="a in alerts" :key="a.id" class="alert-row" :class="{ unseen: !a.seen }">
              <div class="d-flex justify-space-between ga-2">
                <strong>{{ ALERT_LABELS[a.type] ?? a.type }}</strong>
                <span class="text-body-2 muted text-no-wrap">{{ formatDate(a.occurredAt) }}</span>
              </div>
              <p class="text-body-2 mb-1">{{ a.message }}</p>
              <v-btn v-if="!a.seen" variant="text" size="small" class="px-0" @click="markSeen(a.id)">
                Označi kao pročitano
              </v-btn>
            </div>
          </v-card-text>
        </v-card>
      </v-col>
    </v-row>

    <template v-if="recommendations.length">
      <h2 class="section-title">Preporuke iz dnevnika</h2>
      <v-card>
        <v-card-text>
          <RecommendationItem v-for="r in recommendations" :key="r.id" :recommendation="r" />
        </v-card-text>
      </v-card>
    </template>

    <h2 class="section-title">Unosi</h2>
    <v-card>
      <p v-if="!entries.length" class="pa-4 muted mb-0">Dnevnik je prazan.</p>
      <div v-else class="table-wrap">
        <v-table>
          <thead>
            <tr>
              <th>Vreme</th>
              <th>Vrsta</th>
              <th>Detalji</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="e in entries" :key="e.id">
              <td class="text-no-wrap">{{ formatDate(e.occurredAt) }}</td>
              <td>{{ ENTRY_TYPE_LABELS[e.type] }}</td>
              <td>{{ details(e) }}</td>
            </tr>
          </tbody>
        </v-table>
      </div>
    </v-card>

    <div class="mt-6">
      <v-btn variant="text" size="small" :loading="demoLoading" @click="demoWeek">
        Demo: popuni nedelju flushinga i sunca
      </v-btn>
    </div>
  </section>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onBeforeUnmount } from 'vue'
import EntryForm from '~/components/diary/EntryForm.vue'
import { ApiError } from '~/composables/useApi'
import type { CepAlert, DiaryEntry, DiaryEntryResponse, Recommendation } from '~/types/api'

definePageMeta({ roles: ['USER'] })

const diaryApi = useDiaryApi()

const tab = ref<'FLUSHING' | 'SUN' | 'NEW_PRODUCT' | 'SYMPTOM'>('FLUSHING')
const entries = ref<DiaryEntry[]>([])
const alerts = ref<CepAlert[]>([])
const recommendations = ref<Recommendation[]>([])
const lastAlerts = ref<CepAlert[] | null>(null)
const error = ref('')
const demoLoading = ref(false)
let timer: ReturnType<typeof setInterval> | undefined

const unseen = computed(() => alerts.value.filter((a) => !a.seen).length)

async function load() {
  try {
    const [list, alertList, recs] = await Promise.all([
      diaryApi.list(),
      diaryApi.alerts(),
      diaryApi.recommendations(),
    ])
    entries.value = list
    alerts.value = alertList
    recommendations.value = recs
  } catch (e) {
    error.value = e instanceof ApiError ? e.message : 'Dnevnik nije učitan'
  }
}

async function refreshAlerts() {
  try {
    alerts.value = await diaryApi.alerts()
  } catch {
    // the next poll will try again
  }
}

onMounted(() => {
  load()
  timer = setInterval(refreshAlerts, 5000)
})

onBeforeUnmount(() => clearInterval(timer))

async function onSaved(response: DiaryEntryResponse) {
  lastAlerts.value = response.alerts
  await load()
}

async function markSeen(id: number) {
  await diaryApi.markSeen(id)
  await refreshAlerts()
}

async function demoWeek() {
  demoLoading.value = true
  error.value = ''
  try {
    const response = await diaryApi.demoWeek()
    lastAlerts.value = response.alerts
    await load()
  } catch (e) {
    error.value =
      e instanceof ApiError && e.status === 404 ? 'Demo podaci su dostupni samo u dev profilu servera.' : 'Demo nije uspeo'
  } finally {
    demoLoading.value = false
  }
}

function details(e: DiaryEntry): string {
  if (e.type === 'FLUSHING') {
    const trigger = e.trigger ? `, okidač: ${label(FLUSH_TRIGGERS, e.trigger).toLowerCase()}` : ''
    return `${e.durationMinutes} min${trigger}`
  }
  if (e.type === 'SUN') {
    return `UV ${e.uvIndex}, ${e.minutesInSun} min, SPF ${e.sunscreenSpf}`
  }
  if (e.type === 'NEW_PRODUCT') {
    return e.productName ?? ''
  }
  return `${label(SYMPTOM_TYPES, e.symptomType)}, ${label(BODY_AREAS, e.symptomLocation).toLowerCase()}`
}
</script>

<style scoped>
.alert-row {
  padding: 12px 0;
  border-bottom: 1px solid var(--dw-border);
}

.alert-row:last-child {
  border-bottom: none;
}

.alert-row.unseen {
  border-left: 3px solid rgb(var(--v-theme-warning));
  padding-left: 10px;
}

.table-wrap {
  overflow-x: auto;
}
</style>
