<template>
  <details class="how">
    <summary>Kako ovo radi: {{ method }}</summary>
    <div class="how-body">
      <p class="mb-2"><slot /></p>
      <p v-if="files.length" class="files mb-0">
        <span class="label">Fajlovi</span>
        <code v-for="file in files" :key="file">{{ file }}</code>
      </p>
    </div>
  </details>
</template>

<script setup lang="ts">
withDefaults(defineProps<{ method: string; files?: string[] }>(), { files: () => [] })
</script>

<style scoped>
.how {
  border: 1px solid var(--dw-border);
  border-radius: 6px;
  background: #fff;
  margin-bottom: 24px;
  font-size: 0.9rem;
}

summary {
  cursor: pointer;
  padding: 10px 16px;
  font-weight: 600;
  color: rgb(var(--v-theme-primary));
}

summary:focus-visible {
  outline: 2px solid rgb(var(--v-theme-primary));
  outline-offset: 2px;
}

.how-body {
  padding: 0 16px 14px;
  color: #334155;
  max-width: 85ch;
}

.files {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 6px;
}

.label {
  font-weight: 600;
  margin-right: 4px;
}

code {
  font-size: 0.8rem;
  background: #f1f5f9;
  padding: 1px 6px;
  border-radius: 4px;
}
</style>
