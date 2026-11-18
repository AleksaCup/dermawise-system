<template>
  <dl class="rule-source">
    <template v-if="rule">
      <dt>Pravilo</dt>
      <dd class="rule">{{ rule }}</dd>
    </template>
    <template v-if="source">
      <dt>Izvor</dt>
      <dd>{{ source }}</dd>
    </template>
  </dl>
</template>

<script setup lang="ts">
defineProps<{ rule?: string | null; source?: string | null }>()
</script>

<style scoped>
.rule-source {
  display: grid;
  grid-template-columns: max-content 1fr;
  gap: 2px 12px;
  margin: 8px 0 0;
  font-size: 0.8125rem;
}

dt {
  font-weight: 600;
  color: rgb(var(--v-theme-primary));
}

dd {
  margin: 0;
  color: #475569;
}

dd.rule {
  font-weight: 700;
  color: #0f172a;
}
</style>
