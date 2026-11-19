<template>
  <v-row>
    <v-col cols="12" md="6">
      <v-card class="h-100">
        <v-card-title>Fototip i navike</v-card-title>
        <v-card-text>
          <dl class="facts">
            <dt>Reakcija na sunce</dt>
            <dd>{{ label(SUN_REACTIONS, q.phototype.sunReaction) }}</dd>
            <dt>Preplanulost</dt>
            <dd>{{ label(TAN_TENDENCIES, q.phototype.tanTendency) }}</dd>
            <dt>Koža, oči, kosa</dt>
            <dd>
              {{ label(SKIN_COLORS, q.phototype.skinColor) }}, {{ label(EYE_COLORS, q.phototype.eyeColor).toLowerCase() }},
              {{ label(HAIR_COLORS, q.phototype.hairColor).toLowerCase() }}
            </dd>
            <dt>Poreklo</dt>
            <dd>{{ label(ETHNIC_ORIGINS, q.phototype.ethnicOrigin) }}</dd>
            <dt>Trudnoća, dojenje</dt>
            <dd>{{ yesNo(q.lifestyle.pregnant) }}, {{ yesNo(q.lifestyle.breastfeeding).toLowerCase() }}</dd>
            <dt>Hormonska terapija</dt>
            <dd>{{ yesNo(q.lifestyle.hormonalTherapy) }}</dd>
            <dt>Sunce dnevno</dt>
            <dd class="tabular">{{ q.lifestyle.dailySunMinutes }} min</dd>
            <dt>Alergije</dt>
            <dd>{{ q.lifestyle.allergies.join(', ') || 'nema' }}</dd>
            <dt>Proizvodi</dt>
            <dd>{{ q.lifestyle.currentProducts.map((p) => p.name).join(', ') || 'nema' }}</dd>
          </dl>
        </v-card-text>
      </v-card>
    </v-col>
    <v-col cols="12" md="6">
      <v-card class="mb-4">
        <v-card-title>Lezije po regionima (GAGS)</v-card-title>
        <v-card-text>
          <dl class="facts">
            <template v-for="r in FACE_REGIONS" :key="r.value">
              <dt>{{ r.title }}</dt>
              <dd>{{ label(LESION_TYPES, q.lesions[r.value]) }}</dd>
            </template>
          </dl>
        </v-card-text>
      </v-card>
      <v-card>
        <v-card-title>Rozaceja i SCORAD</v-card-title>
        <v-card-text>
          <p class="mb-2">{{ rosaceaSigns.length ? rosaceaSigns.join(', ') : 'Bez znakova rozaceje' }}</p>
          <p class="text-body-2 muted tabular mb-0">
            Flushing {{ q.rosacea.flushingPerWeek }}x nedeljno, papule {{ q.rosacea.papuleCount }}, pustule
            {{ q.rosacea.pustuleCount }}. SCORAD površina {{ q.scorad.affectedAreaPercent }}%, svrab
            {{ q.scorad.pruritus }}/10.
          </p>
        </v-card-text>
      </v-card>
    </v-col>
  </v-row>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { Questionnaire } from '~/types/api'

const props = defineProps<{ q: Questionnaire }>()

function yesNo(value: boolean) {
  return value ? 'Da' : 'Ne'
}

const rosaceaSigns = computed(() => {
  const r = props.q.rosacea
  const signs: [boolean, string][] = [
    [r.persistentErythema, 'trajno crvenilo'],
    [r.centrofacialDistribution, 'na sredini lica'],
    [r.telangiectasia, 'telangiektazije'],
    [r.papulesPustulesWithoutComedones, 'papule i pustule bez komedona'],
    [r.ocularSymptoms, 'simptomi oka'],
    [r.phymatousChanges, 'zadebljanje kože'],
    [r.perioralDistribution, 'oko usta'],
    [r.recentTopicalSteroid, 'skorašnji kortikosteroid'],
  ]
  return signs.filter(([on]) => on).map(([, text]) => text)
})
</script>

<style scoped>
.facts {
  display: grid;
  grid-template-columns: minmax(120px, 40%) 1fr;
  gap: 6px 16px;
  margin: 0;
}

.facts dt {
  color: #475569;
}

.facts dd {
  margin: 0;
}
</style>
