<template>
  <div>
    <v-row>
      <v-col cols="12" md="4">
        <v-card class="h-100">
          <v-card-title>Fototip</v-card-title>
          <v-card-text>
            <div class="big">{{ roman(result.fototip) }}</div>
            <div class="muted tabular">MED {{ result.med }} mJ/cm²</div>
          </v-card-text>
        </v-card>
      </v-col>
      <v-col cols="12" md="8">
        <v-card class="h-100">
          <v-card-title>Dijagnoza</v-card-title>
          <v-card-text>
            <p v-if="!result.diagnoses.length" class="mb-0">
              Nije utvrđeno nijedno od stanja koja sistem prepoznaje.
            </p>
            <div v-for="d in result.diagnoses" :key="d.condition + (d.subtype ?? '')" class="diagnosis">
              <span class="font-weight-bold">{{ CONDITION_LABELS[d.condition] }}</span>
              <span v-if="d.subtype">, {{ SUBTYPE_LABELS[d.subtype] }}</span>
              <span v-if="d.severity" class="muted">, {{ SEVERITY_LABELS[d.severity] }}</span>
              <span v-if="d.score !== null" class="muted tabular"> · {{ scoreLabel(d.condition, d.score) }}</span>
            </div>
          </v-card-text>
        </v-card>
      </v-col>
    </v-row>

    <v-alert v-for="r in result.referrals" :key="r.specialist + r.forCondition" type="warning" class="mt-4">
      <strong>Preporučen pregled: {{ r.specialist }}.</strong> {{ r.reason }}
    </v-alert>

    <h2 class="section-title">Rutina</h2>
    <v-row>
      <v-col cols="12" md="6">
        <v-card class="h-100">
          <v-card-title>Jutarnja rutina</v-card-title>
          <v-card-text>
            <RecommendationItem v-for="r in result.morningRoutine" :key="r.id" :recommendation="r" />
          </v-card-text>
        </v-card>
      </v-col>
      <v-col cols="12" md="6">
        <v-card class="h-100">
          <v-card-title>Večernja rutina</v-card-title>
          <v-card-text>
            <RecommendationItem v-for="r in result.eveningRoutine" :key="r.id" :recommendation="r" />
          </v-card-text>
        </v-card>
      </v-col>
    </v-row>

    <template v-if="result.notInRoutine.length">
      <h2 class="section-title">Povučeno iz rutine</h2>
      <v-card>
        <v-card-text>
          <RecommendationItem v-for="r in result.notInRoutine" :key="r.id" :recommendation="r" />
        </v-card-text>
      </v-card>
    </template>

    <template v-if="result.safetyNotes.length">
      <h2 class="section-title">Bezbednosne napomene</h2>
      <v-card>
        <v-list lines="two">
          <v-list-item v-for="(note, i) in result.safetyNotes" :key="i" :title="note.text" :subtitle="note.source" />
        </v-list>
      </v-card>
    </template>

    <template v-if="result.contactDermatitisCauses.length">
      <h2 class="section-title">Verovatni uzročnici kontaktne reakcije</h2>
      <v-card>
        <v-card-text>
          <v-chip v-for="cause in result.contactDermatitisCauses" :key="cause" class="mr-2 mb-2">{{ cause }}</v-chip>
        </v-card-text>
      </v-card>
    </template>

    <template v-if="result.excludedDiagnoses.length">
      <h2 class="section-title">Isključene dijagnoze</h2>
      <v-card>
        <v-list lines="two">
          <v-list-item
            v-for="e in result.excludedDiagnoses"
            :key="e.condition"
            :title="CONDITION_LABELS[e.condition]"
            :subtitle="e.reason"
          />
        </v-list>
      </v-card>
    </template>

    <h2 class="section-title">Obrazloženje</h2>
    <v-expansion-panels variant="accordion" class="explanation">
      <v-expansion-panel elevation="0">
        <v-expansion-panel-title>
          Okinuta pravila redom ({{ result.explanation.length }})
        </v-expansion-panel-title>
        <v-expansion-panel-text>
          <div class="table-wrap">
            <v-table>
              <thead>
                <tr>
                  <th class="tabular">#</th>
                  <th>Faza</th>
                  <th>Pravilo</th>
                  <th>Opis</th>
                  <th>Izvor</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="(s, i) in result.explanation" :key="i">
                  <td class="tabular muted">{{ i + 1 }}</td>
                  <td>{{ PHASE_LABELS[s.agendaGroup] ?? s.agendaGroup }}</td>
                  <td>{{ s.ruleName }}</td>
                  <td>{{ s.description }}</td>
                  <td class="muted">{{ s.source }}</td>
                </tr>
              </tbody>
            </v-table>
          </div>
        </v-expansion-panel-text>
      </v-expansion-panel>
    </v-expansion-panels>
  </div>
</template>

<script setup lang="ts">
import type { AnalysisResponse } from '~/types/api'

defineProps<{ result: AnalysisResponse }>()
</script>

<style scoped>
.big {
  font-size: 2.5rem;
  font-weight: 700;
  line-height: 1.1;
}

.diagnosis + .diagnosis {
  margin-top: 6px;
}

.explanation {
  border: 1px solid var(--dw-border);
  border-radius: 4px;
}

.table-wrap {
  overflow-x: auto;
}
</style>
