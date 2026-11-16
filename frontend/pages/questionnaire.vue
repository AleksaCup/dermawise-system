<template>
  <section>
    <h1 class="page-title">Upitnik</h1>
    <p class="page-lead">
      Pitanja prate dermatološke instrumente: Fitzpatrick, GAGS, NRS i SCORAD. Odgovori se čuvaju i mogu
      se kasnije menjati.
    </p>

    <div v-if="loading" class="py-12 text-center">
      <v-progress-circular indeterminate color="primary" aria-label="Učitavanje" />
    </div>

    <template v-else>
      <div class="d-flex justify-space-between align-baseline mb-2">
        <h2 class="text-h6 font-weight-bold">{{ steps[step]!.title }}</h2>
        <span class="muted tabular">Korak {{ step + 1 }} od {{ steps.length }}</span>
      </div>
      <v-progress-linear :model-value="((step + 1) / steps.length) * 100" color="primary" height="4" rounded class="mb-6" />

      <v-card>
        <v-card-text class="pa-6">
          <StepPhototype v-if="step === 0" v-model="form.phototype" :errors="phototypeErrors" />
          <StepLesions v-else-if="step === 1" v-model="form.lesions" />
          <StepRosacea v-else-if="step === 2" v-model="form.rosacea" />
          <StepScorad v-else-if="step === 3" v-model="form.scorad" />
          <StepLifestyle v-else v-model="form.lifestyle" />
        </v-card-text>
      </v-card>

      <v-alert v-if="error" type="error" class="mt-4" role="alert">
        {{ error }}
        <ul v-if="Object.keys(serverErrors).length" class="mt-2 ml-4">
          <li v-for="(message, field) in serverErrors" :key="field">{{ field }}: {{ message }}</li>
        </ul>
      </v-alert>

      <div class="d-flex justify-space-between mt-6">
        <v-btn variant="outlined" :disabled="step === 0" @click="step--">Nazad</v-btn>
        <v-btn v-if="step < steps.length - 1" color="primary" @click="next">Dalje</v-btn>
        <v-btn v-else color="primary" :loading="saving" @click="save">Sačuvaj i pokreni analizu</v-btn>
      </div>
    </template>
  </section>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import StepLesions from '~/components/questionnaire/StepLesions.vue'
import StepLifestyle from '~/components/questionnaire/StepLifestyle.vue'
import StepPhototype from '~/components/questionnaire/StepPhototype.vue'
import StepRosacea from '~/components/questionnaire/StepRosacea.vue'
import StepScorad from '~/components/questionnaire/StepScorad.vue'
import { ApiError } from '~/composables/useApi'
import type { IngredientDto, ProductDto, Questionnaire } from '~/types/api'

definePageMeta({ roles: ['USER'] })

const profileApi = useProfileApi()
const analysisApi = useAnalysisApi()

const steps = [
  { title: 'Fototip' },
  { title: 'Akne po regionima' },
  { title: 'Rozaceja' },
  { title: 'Atopijski dermatitis' },
  { title: 'Životne navike i kontraindikacije' },
]

const step = ref(0)
const loading = ref(true)
const saving = ref(false)
const error = ref('')
const serverErrors = ref<Record<string, string>>({})
const phototypeErrors = ref<Record<string, string>>({})
const form = ref<Questionnaire>(emptyQuestionnaire())

onMounted(async () => {
  try {
    const profile = await profileApi.get()
    form.value = profile.questionnaire
  } catch (e) {
    if (!(e instanceof ApiError && e.status === 404)) {
      error.value = e instanceof ApiError ? e.message : 'Profil nije učitan'
    }
  } finally {
    loading.value = false
  }
})

function validatePhototype() {
  const required = {
    sunReaction: 'Izaberite reakciju na sunce',
    tanTendency: 'Izaberite sklonost ka preplanulosti',
    skinColor: 'Izaberite boju kože',
    eyeColor: 'Izaberite boju očiju',
    hairColor: 'Izaberite boju kose',
    ethnicOrigin: 'Izaberite poreklo',
  } as const
  const errors: Record<string, string> = {}
  for (const [field, message] of Object.entries(required)) {
    if (!form.value.phototype[field as keyof typeof required]) {
      errors[field] = message
    }
  }
  phototypeErrors.value = errors
  return Object.keys(errors).length === 0
}

function next() {
  if (step.value === 0 && !validatePhototype()) {
    return
  }
  step.value++
}

function cleanedProducts() {
  return form.value.lifestyle.currentProducts
    .filter((p: ProductDto) => p.name.trim())
    .map((p: ProductDto) => ({ ...p, ingredients: p.ingredients.filter((i: IngredientDto) => i.name.trim()) }))
}

async function save() {
  if (!validatePhototype()) {
    step.value = 0
    return
  }
  saving.value = true
  error.value = ''
  serverErrors.value = {}
  try {
    const payload = { ...form.value, lifestyle: { ...form.value.lifestyle, currentProducts: cleanedProducts() } }
    await profileApi.saveQuestionnaire(payload)
    await analysisApi.run()
    await navigateTo('/results')
  } catch (e) {
    if (e instanceof ApiError) {
      error.value = e.message
      serverErrors.value = e.fieldErrors
    }
  } finally {
    saving.value = false
  }
}
</script>
