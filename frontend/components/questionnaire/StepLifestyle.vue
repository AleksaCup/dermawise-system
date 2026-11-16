<template>
  <div>
    <p class="muted mb-6">Kontraindikacije, alergije i proizvodi koje trenutno koristite.</p>

    <v-row>
      <v-col cols="12" md="6">
        <v-checkbox v-model="model.pregnant" label="Trudnoća" />
        <v-checkbox v-model="model.breastfeeding" label="Dojenje" />
        <v-checkbox v-model="model.hormonalTherapy" label="Hormonska terapija (npr. kontraceptivi)" />
      </v-col>
      <v-col cols="12" md="6">
        <v-text-field
          v-model.number="model.dailySunMinutes"
          type="number"
          min="0"
          max="1440"
          label="Koliko minuta dnevno ste na suncu"
        />
        <v-combobox
          v-model="model.allergies"
          label="Poznate alergije na sastojke"
          hint="Upišite naziv sastojka i pritisnite Enter, npr. methylisothiazolinone"
          persistent-hint
          multiple
          chips
          closable-chips
        />
      </v-col>
    </v-row>

    <h3 class="section-title">Proizvodi koje koristite</h3>
    <p v-if="!model.currentProducts.length" class="muted">Nema dodatih proizvoda.</p>
    <ProductEditor
      v-for="(product, index) in model.currentProducts"
      :key="index"
      v-model="model.currentProducts[index]!"
      @remove="model.currentProducts.splice(index, 1)"
    />
    <v-btn variant="outlined" :prepend-icon="mdiPlus" @click="addProduct">Dodaj proizvod</v-btn>
  </div>
</template>

<script setup lang="ts">
import { mdiPlus } from '@mdi/js'
import type { Questionnaire } from '~/types/api'

const model = defineModel<Questionnaire['lifestyle']>({ required: true })

function addProduct() {
  model.value.currentProducts.push({ name: '', ingredients: [{ name: '', concentration: null }], applicationAreas: [] })
}
</script>
