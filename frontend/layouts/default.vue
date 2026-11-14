<template>
  <v-app>
    <v-app-bar density="comfortable" color="surface">
      <v-container class="d-flex align-center py-0" style="max-width: 1100px">
        <NuxtLink to="/" class="brand">DermaWise</NuxtLink>
      </v-container>
    </v-app-bar>
    <v-main>
      <v-container class="py-8" style="max-width: 1100px">
        <slot />
      </v-container>
    </v-main>
  </v-app>
</template>

<style scoped>
.brand {
  font-weight: 700;
  font-size: 1.15rem;
  color: rgb(var(--v-theme-on-surface));
  text-decoration: none;
}
</style>
