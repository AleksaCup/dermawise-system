<template>
  <section>
    <div class="d-flex justify-space-between align-start flex-wrap ga-4 mb-6">
      <div>
        <h1 class="page-title">Rezultati analize</h1>
        <p v-if="result" class="muted mb-0">Analiza od {{ formatDate(result.createdAt) }}</p>
      </div>
      <div class="d-flex ga-2">
        <v-btn to="/questionnaire" variant="outlined">Izmeni upitnik</v-btn>
        <v-btn color="primary" :loading="running" :disabled="!hasProfile" @click="run">Nova analiza</v-btn>
      </div>
    </div>

    <HowItWorks
      method="forward chaining u šest nivoa"
      :files="['phototype.drl', 'diagnosis.drl', 'therapy.drl', 'safety.drl', 'SeverityCategory.drt', 'MaxConcentrationByPhototype.drt']"
    >
      Odgovori iz upitnika ulaze u sesiju kao činjenice. Pravila su podeljena u četiri faze (agenda grupe: fototip,
      dijagnostika, terapija, bezbednost), a lanac zaključivanja ima šest nivoa: upitnik spreman, fototip i MED, GAGS i
      SCORAD skor (accumulate), kategorija težine (template) i dijagnoza, terapija i na kraju bezbednost (trudnoća,
      alergije, razdvajanje benzoil peroksida i retinoida, maksimalne koncentracije iz template-a). Svaki nivo koristi
      činjenice koje je napravio prethodni. Pod Obrazloženje je spisak svih okinutih pravila.
    </HowItWorks>

    <v-alert v-if="error" type="error" class="mb-6" role="alert">{{ error }}</v-alert>

    <div v-if="loading" class="py-12 text-center">
      <v-progress-circular indeterminate color="primary" aria-label="Učitavanje" />
    </div>

    <v-card v-else-if="!result" class="pa-6">
      <h2 class="text-h6 font-weight-bold mb-2">Još nema analize</h2>
      <p class="muted">Popunite upitnik, a sistem će odrediti fototip, dijagnozu i rutinu.</p>
      <v-btn to="/questionnaire" color="primary">Popuni upitnik</v-btn>
    </v-card>

    <AnalysisView v-else :result="result" />
  </section>
</template>

<script setup lang="ts">
import HowItWorks from '~/components/HowItWorks.vue'
import { ref, onMounted } from 'vue'
import { ApiError } from '~/composables/useApi'
import type { AnalysisResponse } from '~/types/api'

definePageMeta({ roles: ['USER'] })

const analysisApi = useAnalysisApi()
const profileApi = useProfileApi()

const result = ref<AnalysisResponse | null>(null)
const hasProfile = ref(false)
const loading = ref(true)
const running = ref(false)
const error = ref('')

onMounted(async () => {
  try {
    hasProfile.value = (await profileApi.get()) !== null
    if (hasProfile.value) {
      result.value = await analysisApi.latest()
    }
  } catch (e) {
    error.value = e instanceof ApiError ? e.message : 'Rezultati nisu učitani'
  } finally {
    loading.value = false
  }
})

async function run() {
  running.value = true
  error.value = ''
  try {
    result.value = await analysisApi.run()
  } catch (e) {
    error.value = e instanceof ApiError ? e.message : 'Analiza nije uspela'
  } finally {
    running.value = false
  }
}
</script>
