<template>
  <section>
    <h1 class="page-title">DermaWise</h1>
    <p class="page-lead">
      Ekspertski sistem za dijagnostiku stanja kože i preporuku terapije. Svaka dijagnoza i preporuka
      prati priznate dermatološke skale i smernice, uz obrazloženje i izvor.
    </p>

    <v-row>
      <v-col v-for="item in items" :key="item.title" cols="12" md="4">
        <v-card class="h-100">
          <v-card-title>{{ item.title }}</v-card-title>
          <v-card-text class="muted">{{ item.text }}</v-card-text>
        </v-card>
      </v-col>
    </v-row>
  </section>
</template>

<script setup lang="ts">
const items = [
  {
    title: 'Upitnik',
    text: 'Fitzpatrick fototip, GAGS za akne, NRS kriterijumi za rozaceju, SCORAD i životne navike.',
  },
  {
    title: 'Analiza i terapija',
    text: 'Dijagnoza sa težinom, isključene dijagnoze, jutarnja i večernja rutina i bezbednosne provere.',
  },
  {
    title: 'Dnevnik kože',
    text: 'Flushing, izlaganje suncu, novi proizvodi i simptomi, sa upozorenjima u realnom vremenu.',
  },
]
</script>
