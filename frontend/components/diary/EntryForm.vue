<template>
  <v-form @submit.prevent="submit">
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
