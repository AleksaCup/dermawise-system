<template>
  <v-card class="mb-4">
    <v-card-text>
      <div class="d-flex align-start ga-2">
        <v-text-field v-model="model.name" label="Naziv proizvoda" class="flex-grow-1" />
        <v-btn v-if="removable" variant="text" color="error" class="mt-2" @click="$emit('remove')">Ukloni</v-btn>
      </div>
      <v-select
        v-model="model.applicationAreas"
        :items="BODY_AREAS"
        label="Gde se nanosi"
        multiple
        chips
        closable-chips
      />
      <div class="text-body-2 mb-2">Sastojci</div>
      <div v-for="(ingredient, index) in model.ingredients" :key="index" class="d-flex ga-2 align-start">
        <v-text-field v-model="ingredient.name" :label="`Sastojak ${index + 1}`" class="flex-grow-1" />
        <v-text-field
          v-model.number="ingredient.concentration"
          type="number"
          min="0"
          max="100"
          label="%"
          style="max-width: 110px"
        />
        <v-btn
          variant="text"
          :icon="mdiClose"
          :aria-label="`Ukloni sastojak ${index + 1}`"
          class="mt-1"
          @click="model.ingredients.splice(index, 1)"
        />
      </div>
      <v-btn variant="text" size="small" :prepend-icon="mdiPlus" @click="model.ingredients.push({ name: '', concentration: null })">
        Dodaj sastojak
      </v-btn>
    </v-card-text>
  </v-card>
</template>

<script setup lang="ts">
import { mdiClose, mdiPlus } from '@mdi/js'
import type { ProductDto } from '~/types/api'

withDefaults(defineProps<{ removable?: boolean }>(), { removable: true })
defineEmits<{ remove: [] }>()
const model = defineModel<ProductDto>({ required: true })
</script>
