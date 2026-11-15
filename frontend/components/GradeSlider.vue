<template>
  <div class="mb-2">
    <div class="d-flex justify-space-between">
      <span class="text-body-2">{{ label }}</span>
      <span class="text-body-2 muted tabular">{{ valueLabel }}</span>
    </div>
    <v-slider
      v-model="model"
      :min="0"
      :max="max"
      :step="1"
      :aria-label="label"
      color="primary"
      show-ticks="always"
      tick-size="3"
      hide-details
    />
  </div>
</template>

<script setup lang="ts">
const props = withDefaults(defineProps<{ label: string; max?: number; labels?: string[] }>(), {
  max: 3,
  labels: () => GRADE_LABELS,
})
const model = defineModel<number>({ required: true })

const valueLabel = computed(() =>
  props.max === 3 ? `${model.value} · ${props.labels[model.value]}` : `${model.value} / ${props.max}`,
)
</script>
