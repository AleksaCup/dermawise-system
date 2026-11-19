<template>
  <div class="item">
    <div class="d-flex justify-space-between align-start ga-2 flex-wrap">
      <div>
        <div class="step">{{ STEP_LABELS[recommendation.step] }}</div>
        <div class="name">
          <span>{{ recommendation.sastojak }}</span><span v-if="recommendation.koncentracija !== null" class="tabular">{{ ` ${recommendation.koncentracija}%` }}</span><span class="form">{{ `, ${recommendation.form}` }}</span>
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
    <RuleSource :rule="recommendation.ruleName" :source="recommendation.source" />
  </div>
</template>

<script setup lang="ts">
import type { Recommendation } from '~/types/api'
import RuleSource from '~/components/RuleSource.vue'

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
  font-size: 0.8125rem;
  font-weight: 600;
  color: #475569;
}

.name {
  font-weight: 700;
}

.form {
  font-weight: 400;
  color: #475569;
}

.warnings {
  margin: 8px 0 0 18px;
  font-size: 0.875rem;
  color: rgb(var(--v-theme-warning));
}

</style>
