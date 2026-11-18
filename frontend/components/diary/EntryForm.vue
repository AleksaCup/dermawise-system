<template>
  <v-form @submit.prevent="submit">
    <p class="muted mb-3">{{ HELP[type] }}</p>
    <div class="presets mb-5">
      <span class="text-body-2 muted">Primeri:</span>
      <v-btn v-for="preset in PRESETS[type]" :key="preset.label" variant="tonal" size="small" @click="preset.apply()">
        {{ preset.label }}
      </v-btn>
    </div>

    <v-text-field v-model="occurredAt" type="datetime-local" label="Vreme" class="mb-2" />

    <template v-if="type === 'FLUSHING'">
      <v-row>
        <v-col cols="12" sm="6">
          <v-text-field v-model.number="flushing.durationMinutes" type="number" min="1" label="Trajanje (minuta)" />
        </v-col>
        <v-col cols="12" sm="6">
          <v-select v-model="flushing.trigger" :items="FLUSH_TRIGGERS" label="Mogući okidač" clearable />
        </v-col>
      </v-row>
    </template>

    <template v-else-if="type === 'SUN'">
      <v-row>
        <v-col cols="12" sm="4">
          <v-text-field v-model.number="sun.uvIndex" type="number" min="0" max="20" step="0.5" label="UV indeks" />
        </v-col>
        <v-col cols="12" sm="4">
          <v-text-field v-model.number="sun.minutesInSun" type="number" min="1" label="Minuta na suncu" />
        </v-col>
        <v-col cols="12" sm="4">
          <v-text-field
            v-model.number="sun.sunscreenSpf"
            type="number"
            min="0"
            label="SPF kreme"
            hint="0 ako krema nije korišćena"
            persistent-hint
          />
        </v-col>
      </v-row>
    </template>

    <template v-else-if="type === 'NEW_PRODUCT'">
      <ProductEditor v-model="product" :removable="false" />
    </template>

    <template v-else>
      <v-row>
        <v-col cols="12" sm="6">
          <v-select v-model="symptom.type" :items="SYMPTOM_TYPES" label="Simptom" />
        </v-col>
        <v-col cols="12" sm="6">
          <v-select v-model="symptom.location" :items="BODY_AREAS" label="Gde" />
        </v-col>
      </v-row>
    </template>

    <v-alert v-if="error" type="error" class="mb-4" role="alert">{{ error }}</v-alert>
    <v-btn type="submit" color="primary" :loading="saving">Sačuvaj unos</v-btn>
  </v-form>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { ApiError } from '~/composables/useApi'
import type { BodyArea, DiaryEntryResponse, FlushTrigger, ProductDto, SymptomType } from '~/types/api'

const props = defineProps<{ type: 'FLUSHING' | 'SUN' | 'NEW_PRODUCT' | 'SYMPTOM' }>()
const emit = defineEmits<{ saved: [response: DiaryEntryResponse] }>()

const diaryApi = useDiaryApi()

const occurredAt = ref(nowForInput())
const saving = ref(false)
const error = ref('')

const flushing = reactive({ durationMinutes: 15, trigger: null as FlushTrigger | null })
const sun = reactive({ uvIndex: 5, minutesInSun: 30, sunscreenSpf: 0 })
const product = ref<ProductDto>({ name: '', ingredients: [{ name: '', concentration: null }], applicationAreas: [] })
const symptom = reactive({ type: null as SymptomType | null, location: null as BodyArea | null })

type EntryType = 'FLUSHING' | 'SUN' | 'NEW_PRODUCT' | 'SYMPTOM'

const HELP: Record<EntryType, string> = {
  FLUSHING:
    'Nagli talas crvenila lica. Četiri epizode u sedam dana daju sumnju na rozaceju, a isti okidač tri puta u 30 dana postaje dominantan okidač.',
  SUN: 'Doza UV zračenja se računa iz UV indeksa, minuta i SPF-a i poredi sa MED vrednošću vašeg fototipa.',
  NEW_PRODUCT:
    'Proizvod uveden u poslednjih 14 dana. Ako se na mestu nanošenja pojavi simptom, a proizvod sadrži sastojak na koji ste alergični, sistem pronalazi uzročnika.',
  SYMPTOM:
    'Crvenilo, peckanje, svrab ili ljuštenje i mesto gde se javlja. Simptom posle novog proizvoda pokreće proveru kontaktnog dermatitisa. Ljuštenje i crvenilo pored nosa, na obrvama ili skalpu ulaze u sledeću analizu kao seboroični dermatitis.',
}

const PRESETS: Record<EntryType, { label: string; apply: () => void }[]> = {
  FLUSHING: [
    { label: 'Posle čaše vina', apply: () => Object.assign(flushing, { durationMinutes: 20, trigger: 'ALKOHOL' }) },
    { label: 'Posle tople kupke', apply: () => Object.assign(flushing, { durationMinutes: 15, trigger: 'TOPLOTA' }) },
    { label: 'Posle ljute hrane', apply: () => Object.assign(flushing, { durationMinutes: 10, trigger: 'HRANA' }) },
  ],
  SUN: [
    { label: 'Plaža bez kreme', apply: () => Object.assign(sun, { uvIndex: 8, minutesInSun: 90, sunscreenSpf: 0 }) },
    { label: 'Šetnja uz SPF 30', apply: () => Object.assign(sun, { uvIndex: 5, minutesInSun: 30, sunscreenSpf: 30 }) },
  ],
  NEW_PRODUCT: [
    {
      label: 'Krema sa Kathon CG',
      apply: () => {
        product.value = {
          name: 'Nova hidratantna krema',
          ingredients: [
            { name: 'kathon cg', concentration: null },
            { name: 'niacinamid', concentration: 4 },
          ],
          applicationAreas: ['CHEEKS'],
        }
      },
    },
    {
      label: 'Serum sa retinolom',
      apply: () => {
        product.value = { name: 'Noćni serum', ingredients: [{ name: 'retinol', concentration: 0.5 }], applicationAreas: ['CHEEKS', 'FOREHEAD'] }
      },
    },
  ],
  SYMPTOM: [
    { label: 'Crvenilo na obrazima', apply: () => Object.assign(symptom, { type: 'ERYTHEMA', location: 'CHEEKS' }) },
    { label: 'Ljuštenje pored nosa', apply: () => Object.assign(symptom, { type: 'SCALING', location: 'NASOLABIAL_FOLDS' }) },
    { label: 'Crvenilo pored nosa', apply: () => Object.assign(symptom, { type: 'ERYTHEMA', location: 'NASOLABIAL_FOLDS' }) },
  ],
}

async function submit() {
  saving.value = true
  error.value = ''
  const time = inputToIso(occurredAt.value)
  try {
    let response: DiaryEntryResponse
    if (props.type === 'FLUSHING') {
      response = await diaryApi.addFlushing({ occurredAt: time, ...flushing })
    } else if (props.type === 'SUN') {
      response = await diaryApi.addSun({ occurredAt: time, ...sun })
    } else if (props.type === 'NEW_PRODUCT') {
      const cleaned = { ...product.value, ingredients: product.value.ingredients.filter((i) => i.name.trim()) }
      response = await diaryApi.addProduct({ occurredAt: time, product: cleaned })
    } else {
      if (!symptom.type || !symptom.location) {
        error.value = 'Izaberite simptom i mesto'
        return
      }
      response = await diaryApi.addSymptom({ occurredAt: time, type: symptom.type, location: symptom.location })
    }
    emit('saved', response)
    occurredAt.value = nowForInput()
  } catch (e) {
    if (e instanceof ApiError) {
      const details = Object.values(e.fieldErrors)
      error.value = details.length ? `${e.message}: ${details.join(', ')}` : e.message
    }
  } finally {
    saving.value = false
  }
}
</script>

<style scoped>
.presets {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px;
}
</style>
