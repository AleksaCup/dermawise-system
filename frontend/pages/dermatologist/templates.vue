<template>
  <section>
    <div class="d-flex justify-space-between align-start flex-wrap ga-4 mb-6">
      <div>
        <h1 class="page-title">Maksimalne koncentracije po fototipu</h1>
        <p class="page-lead mb-0">
          Svaki red je jedno pravilo (template MaxConcentrationByPhototype). Posle izmene baza znanja se ponovo gradi i
          nova pravila važe od sledeće analize, bez restartovanja sistema.
        </p>
      </div>
      <v-btn color="primary" :prepend-icon="mdiPlus" @click="openNew">Dodaj red</v-btn>
    </div>

    <HowItWorks method="template" :files="['MaxConcentrationByPhototype.drt']">
      Redovi su u bazi podataka. Iz svakog reda se generišu dva pravila za fazu bezbednosti: jedno smanjuje koncentraciju
      preporuke koja prelazi maksimum za fototip, drugo upozorava na proizvod koji korisnik već koristi. Posle izmene se
      sve baze znanja ponovo grade i zamenjuju bez restartovanja servera. Generisana pravila su u pregledu baze znanja.
    </HowItWorks>

    <v-alert v-if="error" type="error" class="mb-4" role="alert">{{ error }}</v-alert>
    <p v-if="message" class="mb-4" role="status" aria-live="polite">{{ message }}</p>

    <v-card>
      <div v-if="loading" class="pa-8 text-center">
        <v-progress-circular indeterminate color="primary" aria-label="Učitavanje" />
      </div>
      <div v-else class="table-wrap">
        <v-table>
          <thead>
            <tr>
              <th>Sastojak</th>
              <th>Fototip</th>
              <th class="text-right">Najviše</th>
              <th>Napomena</th>
              <th>Izmenio</th>
              <th />
            </tr>
          </thead>
          <tbody>
            <tr v-for="row in rows" :key="row.id">
              <td class="font-weight-bold">{{ row.sastojak }}</td>
              <td class="tabular">{{ roman(row.minFototip) }}-{{ roman(row.maxFototip) }}</td>
              <td class="text-right tabular">{{ row.maxKoncentracija }}%</td>
              <td>{{ row.napomena }}</td>
              <td class="text-body-2 muted">{{ row.updatedBy }}<br />{{ formatDate(row.updatedAt) }}</td>
              <td class="text-right text-no-wrap">
                <v-btn variant="text" size="small" @click="openEdit(row)">Izmeni</v-btn>
                <v-btn variant="text" size="small" color="error" @click="toDelete = row">Obriši</v-btn>
              </td>
            </tr>
          </tbody>
        </v-table>
      </div>
    </v-card>

    <h2 class="section-title">Generisana pravila</h2>
    <v-expansion-panels variant="accordion" class="drl">
      <v-expansion-panel elevation="0" @group:selected="loadDrl">
        <v-expansion-panel-title>DRL koji ObjectDataCompiler pravi iz redova</v-expansion-panel-title>
        <v-expansion-panel-text>
          <pre class="code">{{ drl || 'Učitavanje...' }}</pre>
        </v-expansion-panel-text>
      </v-expansion-panel>
    </v-expansion-panels>

    <v-dialog v-model="dialog" max-width="560">
      <v-card>
        <v-card-title>{{ editing ? 'Izmena reda' : 'Novi red' }}</v-card-title>
        <v-card-text>
          <v-form @submit.prevent="save">
            <IngredientField
              v-model="form.sastojak"
              label="Sastojak"
              hint="Naziv kao u preporukama, npr. retinol ili adapalen"
              :kinds="['THERAPY']"
              class="mb-2"
            />
            <v-row>
              <v-col cols="6">
                <v-select v-model="form.minFototip" :items="phototypes" label="Od fototipa" />
              </v-col>
              <v-col cols="6">
                <v-select v-model="form.maxFototip" :items="phototypes" label="Do fototipa" />
              </v-col>
            </v-row>
            <v-text-field v-model.number="form.maxKoncentracija" type="number" min="0" max="100" step="0.01" label="Najveća koncentracija (%)" />
            <v-textarea v-model="form.napomena" label="Napomena za korisnika" rows="2" auto-grow />
            <v-alert v-if="formError" type="error" class="mb-2" role="alert">
              {{ formError }}
              <ul v-if="Object.keys(formFieldErrors).length" class="mt-1 ml-4">
                <li v-for="(m, f) in formFieldErrors" :key="f">{{ f }}: {{ m }}</li>
              </ul>
            </v-alert>
            <div class="d-flex justify-end ga-2">
              <v-btn variant="text" @click="dialog = false">Otkaži</v-btn>
              <v-btn type="submit" color="primary" :loading="saving">Sačuvaj</v-btn>
            </div>
          </v-form>
        </v-card-text>
      </v-card>
    </v-dialog>

    <v-dialog :model-value="toDelete !== null" max-width="440" @update:model-value="toDelete = null">
      <v-card v-if="toDelete">
        <v-card-title>Obrisati red?</v-card-title>
        <v-card-text>
          Pravilo za {{ toDelete.sastojak }} (fototip {{ roman(toDelete.minFototip) }}-{{ roman(toDelete.maxFototip) }})
          više neće važiti.
          <div class="d-flex justify-end ga-2 mt-4">
            <v-btn variant="text" @click="toDelete = null">Otkaži</v-btn>
            <v-btn color="error" :loading="saving" @click="remove">Obriši</v-btn>
          </div>
        </v-card-text>
      </v-card>
    </v-dialog>
  </section>
</template>

<script setup lang="ts">
import HowItWorks from '~/components/HowItWorks.vue'
import { onMounted, ref } from 'vue'
import { mdiPlus } from '@mdi/js'
import { ApiError } from '~/composables/useApi'
import type { TemplateRule, TemplateRuleRequest } from '~/types/api'
import IngredientField from '~/components/IngredientField.vue'

definePageMeta({ roles: ['DERMATOLOGIST'] })

const templateApi = useTemplateApi()
const phototypes = [1, 2, 3, 4, 5, 6].map((v) => ({ value: v, title: roman(v) }))

const rows = ref<TemplateRule[]>([])
const loading = ref(true)
const error = ref('')
const message = ref('')
const drl = ref('')

const dialog = ref(false)
const editing = ref<TemplateRule | null>(null)
const form = ref<TemplateRuleRequest>(emptyForm())
const formError = ref('')
const formFieldErrors = ref<Record<string, string>>({})
const saving = ref(false)
const toDelete = ref<TemplateRule | null>(null)

function emptyForm(): TemplateRuleRequest {
  return { sastojak: '', minFototip: 1, maxFototip: 6, maxKoncentracija: 1, napomena: '' }
}

async function load() {
  try {
    rows.value = await templateApi.list()
  } catch (e) {
    error.value = e instanceof ApiError ? e.message : 'Redovi nisu učitani'
  } finally {
    loading.value = false
  }
}

onMounted(load)

async function loadDrl() {
  if (drl.value) {
    return
  }
  drl.value = (await templateApi.generatedDrl()) || 'Nema redova, pa nema ni generisanih pravila.'
}

function openNew() {
  editing.value = null
  form.value = emptyForm()
  formError.value = ''
  formFieldErrors.value = {}
  dialog.value = true
}

function openEdit(row: TemplateRule) {
  editing.value = row
  form.value = {
    sastojak: row.sastojak,
    minFototip: row.minFototip,
    maxFototip: row.maxFototip,
    maxKoncentracija: row.maxKoncentracija,
    napomena: row.napomena,
  }
  formError.value = ''
  formFieldErrors.value = {}
  dialog.value = true
}

async function afterChange(text: string) {
  message.value = text
  drl.value = ''
  await load()
}

async function save() {
  saving.value = true
  formError.value = ''
  formFieldErrors.value = {}
  try {
    if (editing.value) {
      await templateApi.update(editing.value.id, form.value)
    } else {
      await templateApi.create(form.value)
    }
    dialog.value = false
    await afterChange(`Red za ${form.value.sastojak} je sačuvan i baza znanja je ponovo izgrađena.`)
  } catch (e) {
    if (e instanceof ApiError) {
      formError.value = e.message
      formFieldErrors.value = e.fieldErrors
    }
  } finally {
    saving.value = false
  }
}

async function remove() {
  if (!toDelete.value) {
    return
  }
  saving.value = true
  const row = toDelete.value
  try {
    await templateApi.remove(row.id)
    toDelete.value = null
    await afterChange(`Red za ${row.sastojak} je obrisan i baza znanja je ponovo izgrađena.`)
  } catch (e) {
    error.value = e instanceof ApiError ? e.message : 'Brisanje nije uspelo'
    toDelete.value = null
  } finally {
    saving.value = false
  }
}
</script>

<style scoped>
.table-wrap {
  overflow-x: auto;
}

.drl {
  border: 1px solid var(--dw-border);
  border-radius: 4px;
}

.code {
  font-family: ui-monospace, SFMono-Regular, Menlo, monospace;
  font-size: 0.8rem;
  line-height: 1.5;
  white-space: pre;
  overflow-x: auto;
  max-height: 480px;
  margin: 0;
}
</style>
