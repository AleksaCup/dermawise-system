<template>
  <section>
    <h1 class="page-title">Provera hipoteza</h1>
    <p class="page-lead">
      Umesto pune analize, ovde se proverava jedan cilj: da li nalazi iz upitnika i dnevnika dokazuju određeno stanje i
      šta je verovatan uzročnik reakcije na koži.
    </p>
    <HowItWorks method="backward chaining, rekurzivni upiti" :files="['backward.drl']">
      Sistem kreće od cilja (npr. imaRosaceju) i traži činjenice koje ga dokazuju. Upit dokazano se rekurzivno spušta
      kroz graf nalaza dok ne dođe do nalaza iz upitnika ili dnevnika. Uzročnik kontaktne reakcije se traži upitom
      uzrocnikKontaktnogDermatitisa: novi proizvod, sastojak koji je poznat alergen (PoznatAlergen, rekurzivno kroz graf
      sastojaka i grupa) i simptom na mestu nanošenja.
    </HowItWorks>
    <HypothesisPanel />
  </section>
</template>

<script setup lang="ts">
import HowItWorks from '~/components/HowItWorks.vue'
definePageMeta({ roles: ['USER'] })
</script>
