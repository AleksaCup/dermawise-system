<template>
  <div>
    <p class="muted mb-6">
      Fitzpatrick upitnik. Najvažnije je kako koža reaguje na 30 do 60 minuta podnevnog sunca bez zaštite.
    </p>
    <v-row>
      <v-col cols="12" md="6">
        <v-radio-group v-model="model.sunReaction" label="Reakcija na podnevno sunce" :error-messages="errors.sunReaction">
          <v-radio v-for="o in SUN_REACTIONS" :key="o.value" :label="o.title" :value="o.value" />
        </v-radio-group>
      </v-col>
      <v-col cols="12" md="6">
        <v-radio-group v-model="model.tanTendency" label="Sklonost ka preplanulosti" :error-messages="errors.tanTendency">
          <v-radio v-for="o in TAN_TENDENCIES" :key="o.value" :label="o.title" :value="o.value" />
        </v-radio-group>
      </v-col>
      <v-col cols="12" sm="6">
        <v-select v-model="model.skinColor" :items="SKIN_COLORS" label="Boja kože pre izlaganja suncu" :error-messages="errors.skinColor" />
      </v-col>
      <v-col cols="12" sm="6">
        <v-select v-model="model.eyeColor" :items="EYE_COLORS" label="Boja očiju" :error-messages="errors.eyeColor" />
      </v-col>
      <v-col cols="12" sm="6">
        <v-select v-model="model.hairColor" :items="HAIR_COLORS" label="Prirodna boja kose" :error-messages="errors.hairColor" />
      </v-col>
      <v-col cols="12" sm="6">
        <v-select v-model="model.ethnicOrigin" :items="ETHNIC_ORIGINS" label="Poreklo" :error-messages="errors.ethnicOrigin" />
      </v-col>
    </v-row>
  </div>
</template>

<script setup lang="ts">
import type { Questionnaire } from '~/types/api'

defineProps<{ errors: Record<string, string> }>()
const model = defineModel<Questionnaire['phototype']>({ required: true })
</script>
