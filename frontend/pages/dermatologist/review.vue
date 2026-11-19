<template>
  <section>
    <h1 class="page-title">Preporuke na čekanju</h1>
    <p class="page-lead">
      Preporuke za lekove na recept čekaju vašu potvrdu. Korisnik vidi odluku i komentar u svojoj rutini.
    </p>

    <HowItWorks method="status preporuke" :files="['therapy.drl', 'safety.drl']">
      Pravila terapije označavaju preporuke koje traže recept (npr. azelainska kiselina, ivermektin, brimonidin).
      One dobijaju status PENDING_REVIEW i ne ulaze u rutinu dok ih dermatolog ne odobri.
    </HowItWorks>

    <v-alert v-if="error" type="error" class="mb-6" role="alert">{{ error }}</v-alert>
    <p v-if="message" class="mb-4" role="status" aria-live="polite">{{ message }}</p>

    <div v-if="loading" class="py-12 text-center">
      <v-progress-circular indeterminate color="primary" aria-label="Učitavanje" />
    </div>
    <v-card v-else-if="!items.length" class="pa-6">
      <p class="mb-0 muted">Nema preporuka koje čekaju potvrdu.</p>
    </v-card>

    <v-card v-for="item in items" :key="item.recommendation.id" class="mb-4">
      <v-card-title class="d-flex justify-space-between align-center flex-wrap ga-2">
        <NuxtLink :to="`/dermatologist/patients/${item.patientId}`">{{ item.patientName }}</NuxtLink>
        <v-chip v-if="item.pregnant" color="warning" label>Trudnoća</v-chip>
      </v-card-title>
      <v-card-text>
        <p class="text-body-2 muted mb-0">{{ origin(item) }}</p>
        <RecommendationItem :recommendation="item.recommendation" />
        <v-textarea
          v-model="comments[item.recommendation.id]"
          label="Komentar"
          rows="2"
          auto-grow
          :error-messages="commentErrors[item.recommendation.id] ? [commentErrors[item.recommendation.id]!] : []"
          hint="Obavezan kada odbijate preporuku"
          persistent-hint
        />
        <div class="d-flex ga-2 mt-2">
          <v-btn color="primary" :loading="busy === item.recommendation.id" @click="decide(item, true)">Odobri</v-btn>
          <v-btn variant="outlined" color="error" :disabled="busy === item.recommendation.id" @click="decide(item, false)">
            Odbij
          </v-btn>
        </div>
      </v-card-text>
    </v-card>
  </section>
</template>

<script setup lang="ts">
import HowItWorks from '~/components/HowItWorks.vue'
import { onMounted, ref } from 'vue'
import { ApiError } from '~/composables/useApi'
import type { PendingRecommendation } from '~/types/api'

definePageMeta({ roles: ['DERMATOLOGIST'] })

function origin(item: PendingRecommendation) {
  const parts = [item.recommendation.origin === 'CEP' ? 'Iz dnevnika kože' : 'Iz analize']
  if (item.recommendation.forCondition) {
    parts.push(CONDITION_LABELS[item.recommendation.forCondition] ?? item.recommendation.forCondition)
  }
  parts.push(formatDate(item.recommendation.createdAt))
  return parts.join(', ')
}

const dermatologistApi = useDermatologistApi()

const items = ref<PendingRecommendation[]>([])
const comments = ref<Record<number, string>>({})
const commentErrors = ref<Record<number, string>>({})
const busy = ref<number | null>(null)
const loading = ref(true)
const error = ref('')
const message = ref('')

onMounted(async () => {
  try {
    items.value = await dermatologistApi.pending()
  } catch (e) {
    error.value = e instanceof ApiError ? e.message : 'Preporuke nisu učitane'
  } finally {
    loading.value = false
  }
})

async function decide(item: PendingRecommendation, approve: boolean) {
  const id = item.recommendation.id
  const comment = (comments.value[id] ?? '').trim()
  commentErrors.value = { ...commentErrors.value, [id]: '' }
  if (!approve && !comment) {
    commentErrors.value = { ...commentErrors.value, [id]: 'Napišite zašto odbijate preporuku' }
    return
  }
  busy.value = id
  error.value = ''
  try {
    if (approve) {
      await dermatologistApi.approve(id, comment)
    } else {
      await dermatologistApi.reject(id, comment)
    }
    items.value = items.value.filter((i) => i.recommendation.id !== id)
    message.value = `${item.recommendation.sastojak} za ${item.patientName}: ${approve ? 'odobreno' : 'odbijeno'}.`
  } catch (e) {
    error.value = e instanceof ApiError ? e.message : 'Odluka nije sačuvana'
  } finally {
    busy.value = null
  }
}
</script>
