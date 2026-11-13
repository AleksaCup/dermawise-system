package com.ftn.sbnz.kjar;

import com.ftn.sbnz.kjar.template.MaxConcentrationRows;
import com.ftn.sbnz.model.enums.RecommendationStatus;
import com.ftn.sbnz.model.enums.RoutineSlot;
import com.ftn.sbnz.model.enums.SkinColor;
import com.ftn.sbnz.model.enums.SunReaction;
import com.ftn.sbnz.model.enums.TanTendency;
import com.ftn.sbnz.model.facts.Recommendation;
import com.ftn.sbnz.model.facts.SafetyNote;
import com.ftn.sbnz.model.questionnaire.Product;
import com.ftn.sbnz.model.questionnaire.ProductIngredient;
import com.ftn.sbnz.model.template.MaxConcentrationByPhototype;
import org.junit.jupiter.api.Test;
import org.kie.api.KieBase;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class MaxConcentrationTemplateTest {

    @Test
    void csvHasTheFiveRowsFromTheProposal() {
        List<MaxConcentrationByPhototype> rows = MaxConcentrationRows.load();

        assertThat(rows).extracting(MaxConcentrationByPhototype::getSastojak)
                .containsExactly("hidrohinon", "hidrohinon", "retinol", "salicilna kiselina", "vitamin c");
        assertThat(rows.get(2).getNapomena()).isEqualTo("uvodi se postupno: 2× nedeljno, 3× nedeljno, pa svako veče");
    }

    @Test
    void recommendationAboveMaximumIsLowered() {
        Questionnaires questionnaire = Questionnaires.standard()
                .fact(SafetyRulesTest.extra("retinol", 2.0, RoutineSlot.EVENING));

        try (AnalysisSession session = new AnalysisSession()) {
            session.insertAll(questionnaire.facts()).fire();

            Recommendation retinol = only(session, "retinol");
            assertThat(retinol.getKoncentracija()).isEqualTo(1.0);
            assertThat(retinol.getWarnings()).contains("uvodi se postupno: 2× nedeljno, 3× nedeljno, pa svako veče");
            assertThat(session.all(SafetyNote.class)).extracting(SafetyNote::getText)
                    .anyMatch(t -> t.startsWith("retinol: koncentracija smanjena sa 2.0% na 1.0%"));
            assertThat(session.firedRules()).filteredOn(n -> n.startsWith("Provera bezbedne koncentracije")).hasSize(1);
        }
    }

    @Test
    void hydroquinoneLimitDependsOnPhototype() {
        Questionnaires dark = Questionnaires.standard()
                .sun(SunReaction.NEVER_BURNS, TanTendency.DEEPLY, SkinColor.OLIVE)
                .fact(SafetyRulesTest.extra("hidrohinon", 4.0, RoutineSlot.EVENING));
        Questionnaires light = Questionnaires.standard()
                .sun(SunReaction.ALWAYS_BURNS, TanTendency.MINIMAL, SkinColor.FAIR)
                .fact(SafetyRulesTest.extra("hidrohinon", 4.0, RoutineSlot.EVENING));

        try (AnalysisSession session = new AnalysisSession()) {
            session.insertAll(dark.facts()).fire();
            assertThat(only(session, "hidrohinon").getKoncentracija()).isEqualTo(2.0);
        }
        try (AnalysisSession session = new AnalysisSession()) {
            session.insertAll(light.facts()).fire();
            assertThat(only(session, "hidrohinon").getKoncentracija()).isEqualTo(4.0);
        }
    }

    @Test
    void withdrawnRecommendationIsNotChanged() {
        Questionnaires questionnaire = Questionnaires.standard()
                .fact(SafetyRulesTest.extra("retinol", 2.0, RoutineSlot.EVENING));
        questionnaire.lifestyle().setPregnant(true);

        try (AnalysisSession session = new AnalysisSession()) {
            session.insertAll(questionnaire.facts()).fire();

            Recommendation retinol = only(session, "retinol");
            assertThat(retinol.getStatus()).isEqualTo(RecommendationStatus.WITHDRAWN);
            assertThat(retinol.getKoncentracija()).isEqualTo(2.0);
        }
    }

    @Test
    void productInUseAboveMaximumGivesWarning() {
        Questionnaires questionnaire = Questionnaires.standard()
                .sun(SunReaction.NEVER_BURNS, TanTendency.DEEPLY, SkinColor.OLIVE);
        questionnaire.lifestyle().setCurrentProducts(List.of(new Product("krema za fleke",
                List.of(new ProductIngredient("hidrohinon", 4.0)), Set.of())));

        try (AnalysisSession session = new AnalysisSession()) {
            session.insertAll(questionnaire.facts()).fire();

            assertThat(session.all(SafetyNote.class)).extracting(SafetyNote::getText).containsExactly(
                    "Proizvod krema za fleke sadrži hidrohinon 4.0%, a za fototip 5 preporučeno je najviše 2.0%"
                            + " (smanjena zbog rizika ohronoze)");
        }
    }

    @Test
    void rowAddedAtRuntimeBecomesANewRule() {
        List<MaxConcentrationByPhototype> rows = new ArrayList<>(MaxConcentrationRows.load());
        rows.add(new MaxConcentrationByPhototype("adapalen", 1, 3, 0.05, "niža koncentracija na početku terapije"));

        KieBase rebuilt = new KnowledgeBaseFactory().buildWithMaxConcentration(rows).analysis();

        assertThat(rebuilt.getRule("rules", "Provera bezbedne koncentracije 5")).isNotNull();
        try (AnalysisSession session = new AnalysisSession(rebuilt)) {
            session.insertAll(TherapyRulesTest.moderateAcne().facts()).fire();

            assertThat(only(session, "adapalen").getKoncentracija()).isEqualTo(0.05);
        }
    }

    private static Recommendation only(AnalysisSession session, String sastojak) {
        List<Recommendation> matching = session.all(Recommendation.class).stream()
                .filter(r -> r.getSastojak().equals(sastojak)).toList();
        assertThat(matching).hasSize(1);
        return matching.get(0);
    }
}
