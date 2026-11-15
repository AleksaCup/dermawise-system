<template>
  <div>
    <p class="muted mb-6">
      SCORAD upitnik za atopijski dermatitis. Ako nemate ekcem, ostavite sve na nuli.
    </p>

    <h3 class="section-title mt-0">Zahvaćena površina</h3>
    <div class="d-flex justify-space-between">
      <span class="text-body-2">Procenat kože sa promenama (pravilo devetki)</span>
      <span class="text-body-2 muted tabular">{{ model.affectedAreaPercent }}%</span>
    </div>
    <v-slider
      v-model="model.affectedAreaPercent"
      :min="0"
      :max="100"
      :step="1"
      aria-label="Zahvaćena površina u procentima"
      color="primary"
      hide-details
    />

    <h3 class="section-title">Intenzitet znakova</h3>
    <v-row>
      <v-col cols="12" md="6">
        <GradeSlider v-model="model.erythema" label="Crvenilo" />
        <GradeSlider v-model="model.edemaPapulation" label="Otok i kvržice" />
        <GradeSlider v-model="model.excoriation" label="Ogrebotine od češanja" />
      </v-col>
      <v-col cols="12" md="6">
        <GradeSlider v-model="model.lichenification" label="Zadebljanje kože" />
        <GradeSlider v-model="model.oozingCrusting" label="Vlaženje i kraste" />
        <GradeSlider v-model="model.dryness" label="Suvoća (na zdravoj koži)" />
      </v-col>
    </v-row>

    <h3 class="section-title">Simptomi u poslednja 3 dana</h3>
    <v-row>
      <v-col cols="12" md="6">
        <GradeSlider v-model="model.pruritus" label="Svrab" :max="10" />
      </v-col>
      <v-col cols="12" md="6">
        <GradeSlider v-model="model.sleeplessness" label="Nesanica zbog svraba" :max="10" />
      </v-col>
    </v-row>
  </div>
</template>

<script setup lang="ts">
import type { Questionnaire } from '~/types/api'

const model = defineModel<Questionnaire['scorad']>({ required: true })
</script>
