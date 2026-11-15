<template>
  <v-chip :color="info.color" label>{{ info.label }}</v-chip>
</template>

<script setup lang="ts">
import type { RecommendationStatus } from '~/types/api'

const props = defineProps<{ status: RecommendationStatus }>()
const info = computed(() => STATUS_INFO[props.status] ?? { label: props.status, color: 'default' })
</script>
