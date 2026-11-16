<template>
  <div>
    <v-card class="mb-6">
      <v-card-title>Da li je hipoteza dokazana?</v-card-title>
      <v-card-text>
        <p class="muted mb-4">
          Sistem kreće od cilja i unazad traži nalaze koji ga dokazuju, prema NRS kriterijumima i nalazima iz upitnika i
          dnevnika.
        </p>
        <div class="d-flex flex-wrap ga-2 align-center mb-4">
          <v-btn-toggle v-model="goal" mandatory color="primary" variant="outlined" divided density="comfortable">
            <v-btn v-for="g in GOALS" :key="g.value" :value="g.value">{{ g.title }}</v-btn>
          </v-btn-toggle>
          <v-btn color="primary" :loading="loadingHypothesis" @click="checkHypothesis">Proveri</v-btn>
        </div>

        <div v-if="hypothesis" role="status" aria-live="polite">
          <div class="d-flex align-center ga-2 mb-2">
            <v-icon :icon="hypothesis.proven ? mdiCheckCircleOutline : mdiCloseCircleOutline" :color="hypothesis.proven ? 'success' : 'error'" aria-hidden="true" />
            <strong>{{ goalTitle }}: {{ hypothesis.proven ? 'dokazano' : 'nije dokazano' }}</strong>
            <span class="text-body-2 muted">· upit {{ hypothesis.query }}</span>
          </div>
          <template v-if="hypothesis.proven">
            <p class="text-body-2 mb-2">Dokazano preko:</p>
            <v-chip v-for="f in hypothesis.provenThrough" :key="f" class="mr-2 mb-2">{{ FINDING_LABELS[f] ?? f }}</v-chip>
          </template>
          <p v-else class="text-body-2 muted mb-0">Nalazi u upitniku i dnevniku nisu dovoljni da se cilj dokaže.</p>
        </div>
      </v-card-text>
    </v-card>

    <v-row>
      <v-col cols="12" md="6">
        <v-card class="h-100">
          <v-card-title>Uzročnik kontaktne reakcije</v-card-title>
          <v-card-text>
            <p class="muted mb-4">
              Traži sastojak iz proizvoda uvedenog u poslednjih 14 dana koji je poznat alergen (ili sa njim unakrsno
              reaguje), uz simptom na mestu gde se proizvod nanosi.
            </p>
            <v-btn color="primary" variant="outlined" :loading="loadingCause" @click="findCause">Pronađi uzročnika</v-btn>
            <div v-if="causes !== null" class="mt-4" role="status" aria-live="polite">
              <p v-if="!causes.length" class="muted mb-0">Uzročnik nije pronađen.</p>
              <v-table v-else density="compact">
                <thead>
                  <tr>
                    <th>Sastojak</th>
                    <th>Proizvod</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="c in causes" :key="c.sastojak + c.proizvod">
                    <td>{{ c.sastojak }}</td>
                    <td>{{ c.proizvod }}</td>
                  </tr>
                </tbody>
              </v-table>
            </div>
          </v-card-text>
        </v-card>
      </v-col>

      <v-col cols="12" md="6">
        <v-card class="h-100">
          <v-card-title>Provera sastojka</v-card-title>
          <v-card-text>
            <p class="muted mb-4">Da li je sastojak poznat alergen i kojim grupama pripada.</p>
            <v-form class="d-flex ga-2 align-start" @submit.prevent="checkAllergen">
              <v-text-field v-model="ingredient" label="Naziv sastojka" hide-details class="flex-grow-1" />
              <v-btn type="submit" color="primary" variant="outlined" class="mt-2" :loading="loadingAllergen" :disabled="!ingredient.trim()">
                Proveri
              </v-btn>
            </v-form>
            <div v-if="allergen" class="mt-4" role="status" aria-live="polite">
              <p class="mb-2">
                <strong>{{ allergen.sastojak }}</strong>
                {{ allergen.knownAllergen ? 'jeste poznat alergen za ovog korisnika.' : 'nije poznat alergen za ovog korisnika.' }}
              </p>
              <p v-if="allergen.groups.length" class="text-body-2 mb-0">
                Grupe: <v-chip v-for="g in allergen.groups" :key="g" class="mr-1">{{ g }}</v-chip>
              </p>
            </div>
          </v-card-text>
        </v-card>
      </v-col>
    </v-row>

    <v-alert v-if="error" type="error" class="mt-6" role="alert">{{ error }}</v-alert>
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { mdiCheckCircleOutline, mdiCloseCircleOutline } from '@mdi/js'
import { ApiError } from '~/composables/useApi'
import type { AllergenCheck, ContactCause, HypothesisResponse } from '~/types/api'

const props = defineProps<{ patientId?: number }>()

const backwardApi = useBackwardApi()

const goal = ref('rozaceja')
const hypothesis = ref<HypothesisResponse | null>(null)
const causes = ref<ContactCause[] | null>(null)
const ingredient = ref('')
const allergen = ref<AllergenCheck | null>(null)
const loadingHypothesis = ref(false)
const loadingCause = ref(false)
const loadingAllergen = ref(false)
const error = ref('')

const goalTitle = computed(() => GOALS.find((g) => g.value === hypothesis.value?.goal)?.title ?? '')

async function guarded(loading: { value: boolean }, action: () => Promise<void>) {
  loading.value = true
  error.value = ''
  try {
    await action()
  } catch (e) {
    error.value = e instanceof ApiError ? e.message : 'Provera nije uspela'
  } finally {
    loading.value = false
  }
}

function checkHypothesis() {
  return guarded(loadingHypothesis, async () => {
    hypothesis.value = await backwardApi.hypothesis(goal.value, props.patientId)
  })
}

function findCause() {
  return guarded(loadingCause, async () => {
    causes.value = await backwardApi.contactCause(props.patientId)
  })
}

function checkAllergen() {
  return guarded(loadingAllergen, async () => {
    allergen.value = await backwardApi.allergenCheck(ingredient.value.trim(), props.patientId)
  })
}
</script>
