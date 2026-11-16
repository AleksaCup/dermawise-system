<template>
  <div class="item">
    <div class="d-flex justify-space-between align-start ga-2 flex-wrap">
      <div>
        <div class="step">{{ STEP_LABELS[recommendation.step] }}</div>
        <div class="name">
          {{ recommendation.sastojak }}
          <span v-if="recommendation.koncentracija !== null" class="tabular">{{ ` ${recommendation.koncentracija}%` }}</span>
          <span class="muted">{{ ` · ${recommendation.form}` }}</span>
        </div>
        <div class="muted text-body-2">{{ recommendation.frequency }}</div>
      </div>
      <StatusChip v-if="recommendation.status !== 'ACTIVE'" :status="recommendation.status" />
    </div>

    <ul v-if="recommendation.warnings.length" class="warnings">
      <li v-for="warning in recommendation.warnings" :key="warning">{{ warning }}</li>
    </ul>
    <p v-if="recommendation.reviewComment" class="text-body-2 mt-1">
      Komentar dermatologa: {{ recommendation.reviewComment }}
    </p>
    <p class="text-body-2 muted mt-1 mb-0">{{ recommendation.reason }}</p>
    <p class="source mb-0">{{ recommendation.source }} · pravilo „{{ recommendation.ruleName }}”</p>
  </div>
</template>

<script setup lang="ts">
import type { Recommendation } from '~/types/api'

defineProps<{ recommendation: Recommendation }>()
</script>

<style scoped>
.item {
  padding: 16px 0;
  border-bottom: 1px solid var(--dw-border);
}

.item:last-child {
  border-bottom: none;
}

.step {
  font-size: 0.75rem;
  text-transform: uppercase;
  letter-spacing: 0.04em;
  color: #475569;
}

.name {
  font-weight: 700;
}

.warnings {
  margin: 8px 0 0 18px;
  font-size: 0.875rem;
  color: rgb(var(--v-theme-warning));
}

.source {
  font-size: 0.8rem;
  color: #64748b;
  margin-top: 4px;
}
</style>
