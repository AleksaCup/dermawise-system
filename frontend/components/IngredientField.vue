<template>
  <v-combobox
    v-model="value"
    :items="items"
    item-title="name"
    item-value="name"
    :return-object="false"
    :multiple="multiple"
    :chips="multiple"
    :closable-chips="multiple"
    :label="label"
    :hint="hint"
    :persistent-hint="!!hint"
    :hide-details="hint ? false : 'auto'"
    auto-select-first
  >
    <template #item="{ props: itemProps, item }">
      <v-list-item v-bind="itemProps" :subtitle="describe(item.raw)" />
    </template>
  </v-combobox>
</template>

<script setup lang="ts">
import { computed, onMounted } from 'vue'
import type { IngredientKind, IngredientOption } from '~/types/api'

const props = withDefaults(
  defineProps<{ label: string; hint?: string; multiple?: boolean; kinds?: IngredientKind[] }>(),
  { hint: undefined, multiple: false, kinds: undefined },
)
const model = defineModel<string | string[] | null>({ required: true })

const value = computed({
  get: () => model.value,
  set: (v) => {
    model.value = v ?? (props.multiple ? [] : '')
  },
})

const { options, load } = useIngredients()
onMounted(load)

const items = computed(() =>
  props.kinds ? options.value.filter((o) => props.kinds!.includes(o.kind)) : options.value,
)

function describe(option: IngredientOption) {
  const kind = INGREDIENT_KIND_LABELS[option.kind]
  return option.partOf.length ? `${kind}, deo: ${option.partOf.join(', ')}` : kind
}
</script>
