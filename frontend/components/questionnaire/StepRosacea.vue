<template>
  <div>
    <p class="muted mb-6">
      Znaci rozaceje prema NRS klasifikaciji i pitanja koja razlikuju rozaceju od perioralnog dermatitisa.
    </p>

    <h3 class="section-title mt-0">Crvenilo i krvni sudovi</h3>
    <v-row>
      <v-col cols="12" md="6">
        <v-switch v-model="model.persistentErythema" label="Crvenilo lica koje ne prolazi" />
        <v-switch v-model="model.centrofacialDistribution" label="Promene su na sredini lica (obrazi, nos, brada, čelo)" />
        <v-switch v-model="model.telangiectasia" label="Vidljivi sitni krvni sudovi (telangiektazije)" />
      </v-col>
      <v-col cols="12" md="6">
        <v-text-field
          v-model.number="model.flushingPerWeek"
          type="number"
          min="0"
          label="Koliko puta nedeljno imate naglo crvenilo (flushing)"
        />
      </v-col>
    </v-row>

    <h3 class="section-title">Papule i pustule</h3>
    <v-row>
      <v-col cols="12" md="6">
        <v-switch v-model="model.papulesPustulesWithoutComedones" label="Papule i pustule bez mitisera" />
      </v-col>
      <v-col cols="6" md="3">
        <v-text-field v-model.number="model.papuleCount" type="number" min="0" label="Broj papula" />
      </v-col>
      <v-col cols="6" md="3">
        <v-text-field v-model.number="model.pustuleCount" type="number" min="0" label="Broj pustula" />
      </v-col>
    </v-row>

    <h3 class="section-title">Jačina primarnih znakova (0 do 3)</h3>
    <v-row>
      <v-col cols="12" md="6">
        <GradeSlider v-model="model.flushingGrade" label="Flushing" />
        <GradeSlider v-model="model.persistentErythemaGrade" label="Trajno crvenilo" />
      </v-col>
      <v-col cols="12" md="6">
        <GradeSlider v-model="model.telangiectasiaGrade" label="Telangiektazije" />
        <GradeSlider v-model="model.papulesPustulesGrade" label="Papule i pustule" />
      </v-col>
    </v-row>

    <h3 class="section-title">Ostalo</h3>
    <v-row>
      <v-col cols="12" md="6">
        <v-switch v-model="model.ocularSymptoms" label="Peckanje, suvoća ili crvenilo očiju" />
        <v-switch v-model="model.phymatousChanges" label="Zadebljanje kože (najčešće na nosu)" />
      </v-col>
      <v-col cols="12" md="6">
        <v-switch v-model="model.perioralDistribution" label="Promene su oko usta" />
        <v-switch v-model="model.recentTopicalSteroid" label="Skoro sam koristio/la kremu sa kortikosteroidom na licu" />
      </v-col>
    </v-row>
  </div>
</template>

<script setup lang="ts">
import type { Questionnaire } from '~/types/api'

const model = defineModel<Questionnaire['rosacea']>({ required: true })
</script>
