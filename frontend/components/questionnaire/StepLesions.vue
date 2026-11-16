<template>
  <div>
    <p class="muted mb-6">
      GAGS upitnik. Za svaki region izaberite najtežu promenu koja je trenutno prisutna.
    </p>
    <v-row>
      <v-col v-for="region in FACE_REGIONS" :key="region.value" cols="12" sm="6">
        <v-select v-model="model[region.value]" :items="LESION_TYPES" :label="region.title" hide-details />
      </v-col>
    </v-row>
  </div>
</template>

<script setup lang="ts">
import type { Questionnaire } from '~/types/api'

const model = defineModel<Questionnaire['lesions']>({ required: true })
</script>
