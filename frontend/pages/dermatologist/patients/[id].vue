<template>
  <section>
    <NuxtLink to="/dermatologist/patients" class="text-body-2">Nazad na pacijente</NuxtLink>
    <h1 class="page-title mt-2">{{ profile?.fullName ?? 'Pacijent' }}</h1>
    <p v-if="profile" class="page-lead">
      Fototip {{ profile.fototip ? roman(profile.fototip) : 'još nije određen' }}
      <span v-if="profile.med">, MED {{ profile.med }} mJ/cm²</span>
      <span v-if="profile.pregnant">, trudnoća</span>
      <span v-if="profile.breastfeeding">, dojenje</span>
    </p>

    <v-alert v-if="error" type="error" class="mb-6" role="alert">{{ error }}</v-alert>

    <v-tabs v-model="tab" color="primary" class="mb-6">
      <v-tab value="analysis">Analiza</v-tab>
      <v-tab value="questionnaire">Upitnik</v-tab>
      <v-tab value="alerts">Dnevnik</v-tab>
      <v-tab value="hypothesis">Hipoteze</v-tab>
    </v-tabs>

    <div v-if="loading" class="py-12 text-center">
      <v-progress-circular indeterminate color="primary" aria-label="Učitavanje" />
    </div>
    <template v-else>
      <div v-if="tab === 'analysis'">
        <AnalysisView v-if="analysis" :result="analysis" />
        <p v-else class="muted">Pacijent još nema analizu.</p>
      </div>
      <div v-else-if="tab === 'questionnaire'">
        <QuestionnaireSummary v-if="profile" :q="profile.questionnaire" />
        <p v-else class="muted">Pacijent još nije popunio upitnik.</p>
      </div>
      <div v-else-if="tab === 'alerts'">
        <v-card>
          <p v-if="!alerts.length" class="pa-4 muted mb-0">Nema upozorenja iz dnevnika.</p>
          <v-list v-else lines="two">
            <v-list-item
              v-for="a in alerts"
              :key="a.id"
              :title="ALERT_LABELS[a.type] ?? a.type"
              :subtitle="`${formatDate(a.occurredAt)} · ${a.message}`"
            />
          </v-list>
        </v-card>
      </div>
      <HypothesisPanel v-else-if="profile" :patient-id="patientId" />
      <p v-else class="muted">Hipoteze se proveravaju nad upitnikom, a pacijent ga još nije popunio.</p>
    </template>
  </section>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { ApiError } from '~/composables/useApi'
import type { AnalysisResponse, CepAlert, ProfileResponse } from '~/types/api'

definePageMeta({ roles: ['DERMATOLOGIST'] })

const route = useRoute()
const patientId = Number(route.params.id)
const dermatologistApi = useDermatologistApi()

const tab = ref('analysis')
const profile = ref<ProfileResponse | null>(null)
const analysis = ref<AnalysisResponse | null>(null)
const alerts = ref<CepAlert[]>([])
const loading = ref(true)
const error = ref('')

onMounted(async () => {
  try {
    profile.value = await dermatologistApi.profile(patientId)
    analysis.value = await dermatologistApi.analysis(patientId)
    alerts.value = await dermatologistApi.alerts(patientId)
  } catch (e) {
    error.value = e instanceof ApiError ? e.message : 'Podaci o pacijentu nisu učitani'
  } finally {
    loading.value = false
  }
})
</script>
