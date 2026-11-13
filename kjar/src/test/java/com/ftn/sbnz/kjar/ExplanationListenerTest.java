package com.ftn.sbnz.kjar;

import com.ftn.sbnz.kjar.seed.EvidenceGraph;
import com.ftn.sbnz.kjar.seed.IngredientGraph;
import com.ftn.sbnz.kjar.template.MaxConcentrationRows;
import com.ftn.sbnz.model.facts.ExplanationStep;
import org.junit.jupiter.api.Test;
import org.kie.api.runtime.KieSession;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

class ExplanationListenerTest {

    @Test
    void recordsEveryFiredRuleWithPhaseSourceAndDescription() {
        KieSession session = new KnowledgeBaseFactory().buildWithMaxConcentration(MaxConcentrationRows.load())
                .analysis().newKieSession();
        ExplanationListener listener = new ExplanationListener();
        session.addEventListener(listener);
        try {
            EvidenceGraph.edges().forEach(session::insert);
            IngredientGraph.edges().forEach(session::insert);
            IngredientGraph.groups().forEach(session::insert);
            TherapyRulesTest.moderateAcne().facts().forEach(session::insert);
            session.getAgenda().getAgendaGroup("safety").setFocus();
            session.getAgenda().getAgendaGroup("therapy").setFocus();
            session.getAgenda().getAgendaGroup("diagnosis").setFocus();
            session.getAgenda().getAgendaGroup("phototype").setFocus();
            int fired = session.fireAllRules();

            List<ExplanationStep> steps = listener.getSteps();
            assertThat(steps).hasSize(fired);
            assertThat(steps.get(0)).extracting(ExplanationStep::getRuleName, ExplanationStep::getAgendaGroup)
                    .containsExactly("Upitnik spreman", "phototype");
            assertThat(steps).extracting(ExplanationStep::getRuleName, ExplanationStep::getSource)
                    .contains(
                            tuple("GAGS skor", "Doshi 1997, GAGS"),
                            tuple("Kategorija GAGS MODERATE 1", "Doshi 1997 (GAGS)"),
                            tuple("Akne umerene, fototip I-III", "Zaenglein 2016, AAD, Tabela 4"));
            assertThat(steps).allSatisfy(step -> assertThat(step.getDescription()).isNotBlank());
            assertThat(steps).extracting(ExplanationStep::getAgendaGroup)
                    .containsSubsequence("phototype", "diagnosis", "therapy", "safety");
        } finally {
            session.dispose();
        }
    }
}
