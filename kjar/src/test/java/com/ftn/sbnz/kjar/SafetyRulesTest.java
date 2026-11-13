package com.ftn.sbnz.kjar;

import com.ftn.sbnz.model.enums.Condition;
import com.ftn.sbnz.model.enums.RecommendationStatus;
import com.ftn.sbnz.model.enums.RoutineSlot;
import com.ftn.sbnz.model.enums.RoutineStep;
import com.ftn.sbnz.model.facts.Recommendation;
import com.ftn.sbnz.model.facts.SafetyNote;
import com.ftn.sbnz.model.questionnaire.Product;
import com.ftn.sbnz.model.questionnaire.ProductIngredient;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class SafetyRulesTest {

    private static final String U = Questionnaires.USER_ID;

    static Recommendation extra(String sastojak, Double koncentracija, RoutineSlot slot) {
        return Recommendation.builder().userId(U).forCondition(Condition.ACNE).sastojak(sastojak)
                .koncentracija(koncentracija).form("gel").slot(slot).step(RoutineStep.TREATMENT)
                .frequency("1x dnevno").ruleName("test").build();
    }

    @Test
    void pregnancyWithdrawsAdapaleneAndAddsAzelaicAcid() {
        Questionnaires questionnaire = TherapyRulesTest.moderateAcne();
        questionnaire.lifestyle().setPregnant(true);

        try (AnalysisSession session = new AnalysisSession()) {
            session.insertAll(questionnaire.facts()).fire();

            List<Recommendation> all = session.all(Recommendation.class);
            assertThat(all).filteredOn(r -> r.getSastojak().equals("adapalen"))
                    .singleElement().extracting(Recommendation::getStatus).isEqualTo(RecommendationStatus.WITHDRAWN);
            assertThat(all).filteredOn(r -> r.getSastojak().equals("azelainska kiselina"))
                    .singleElement().satisfies(r -> {
                        assertThat(r.getKoncentracija()).isEqualTo(15.0);
                        assertThat(r.getSlot()).isEqualTo(RoutineSlot.EVENING);
                        assertThat(r.getStatus()).isEqualTo(RecommendationStatus.PENDING_REVIEW);
                    });
            assertThat(session.all(SafetyNote.class)).extracting(SafetyNote::getText)
                    .contains("Adapalen povučen - prijavljena trudnoća, zamenjen azelainskom kiselinom 15%");
        }
    }

    @Test
    void breastfeedingWithdrawsTretinoin() {
        Questionnaires questionnaire = Questionnaires.standard().fact(extra("tretinoin", 0.025, RoutineSlot.EVENING));
        questionnaire.lifestyle().setBreastfeeding(true);

        try (AnalysisSession session = new AnalysisSession()) {
            session.insertAll(questionnaire.facts()).fire();

            assertThat(session.all(SafetyNote.class)).extracting(SafetyNote::getText)
                    .contains("Tretinoin povučen - prijavljeno dojenje, zamenjen azelainskom kiselinom 15%");
        }
    }

    @Test
    void pregnancyWithdrawsDoxycycline() {
        Questionnaires questionnaire = Questionnaires.standard().fact(extra("doksiciklin", null, RoutineSlot.MORNING));
        questionnaire.lifestyle().setPregnant(true);

        try (AnalysisSession session = new AnalysisSession()) {
            session.insertAll(questionnaire.facts()).fire();

            assertThat(session.all(Recommendation.class)).filteredOn(r -> r.getSastojak().equals("doksiciklin"))
                    .extracting(Recommendation::getStatus).containsExactly(RecommendationStatus.WITHDRAWN);
            assertThat(session.all(Recommendation.class)).extracting(Recommendation::getSastojak)
                    .doesNotContain("azelainska kiselina");
        }
    }

    @Test
    void noPregnancyNoWithdrawal() {
        try (AnalysisSession session = new AnalysisSession()) {
            session.insertAll(TherapyRulesTest.moderateAcne().facts()).fire();

            assertThat(session.all(Recommendation.class)).extracting(Recommendation::getStatus)
                    .doesNotContain(RecommendationStatus.WITHDRAWN);
        }
    }

    @Test
    void benzoylPeroxideMovesToMorningAndRetinoidStaysInEvening() {
        try (AnalysisSession session = new AnalysisSession()) {
            session.insertAll(TherapyRulesTest.moderateAcne().facts()).fire();

            List<Recommendation> all = session.all(Recommendation.class);
            assertThat(all).filteredOn(r -> r.getSastojak().equals("benzoil peroksid"))
                    .singleElement().extracting(Recommendation::getSlot).isEqualTo(RoutineSlot.MORNING);
            assertThat(all).filteredOn(r -> r.getSastojak().equals("adapalen"))
                    .singleElement().extracting(Recommendation::getSlot).isEqualTo(RoutineSlot.EVENING);
            assertThat(session.timesFired("Benzoil peroksid i retinoid u istoj rutini")).isEqualTo(1);
        }
    }

    @Test
    void bothInMorningSendsRetinoidToEvening() {
        Questionnaires questionnaire = Questionnaires.standard()
                .fact(extra("benzoil peroksid", 2.5, RoutineSlot.MORNING))
                .fact(extra("tretinoin", 0.025, RoutineSlot.MORNING));

        try (AnalysisSession session = new AnalysisSession()) {
            session.insertAll(questionnaire.facts()).fire();

            assertThat(session.all(Recommendation.class)).filteredOn(r -> r.getSastojak().equals("tretinoin"))
                    .singleElement().extracting(Recommendation::getSlot).isEqualTo(RoutineSlot.EVENING);
        }
    }

    @Test
    void pregnancyMeansNoSplitBecauseRetinoidIsGone() {
        Questionnaires questionnaire = TherapyRulesTest.moderateAcne();
        questionnaire.lifestyle().setPregnant(true);

        try (AnalysisSession session = new AnalysisSession()) {
            session.insertAll(questionnaire.facts()).fire();

            assertThat(session.timesFired("Benzoil peroksid i retinoid u istoj rutini")).isZero();
        }
    }

    @Test
    void activeRetinoidGivesOneSunNote() {
        Questionnaires questionnaire = TherapyRulesTest.moderateAcne().fact(extra("retinol", 0.5, RoutineSlot.EVENING));

        try (AnalysisSession session = new AnalysisSession()) {
            session.insertAll(questionnaire.facts()).fire();

            assertThat(session.all(SafetyNote.class)).filteredOn(n -> n.getText().startsWith("Retinoidi stanjuju"))
                    .hasSize(1);
        }
    }

    @Test
    void withdrawnRetinoidGivesNoSunNote() {
        Questionnaires questionnaire = TherapyRulesTest.moderateAcne();
        questionnaire.lifestyle().setPregnant(true);

        try (AnalysisSession session = new AnalysisSession()) {
            session.insertAll(questionnaire.facts()).fire();

            assertThat(session.all(SafetyNote.class)).noneMatch(n -> n.getText().startsWith("Retinoidi stanjuju"));
        }
    }

    @Test
    void allergyWithdrawsRecommendedIngredient() {
        Questionnaires questionnaire = TherapyRulesTest.moderateAcne();
        questionnaire.lifestyle().setAllergies(List.of("benzoil peroksid"));

        try (AnalysisSession session = new AnalysisSession()) {
            session.insertAll(questionnaire.facts()).fire();

            List<Recommendation> all = session.all(Recommendation.class);
            assertThat(all).filteredOn(r -> r.getSastojak().equals("benzoil peroksid"))
                    .extracting(Recommendation::getStatus).containsExactly(RecommendationStatus.WITHDRAWN);
            assertThat(all).filteredOn(r -> r.getSastojak().equals("adapalen"))
                    .extracting(Recommendation::getStatus).containsExactly(RecommendationStatus.ACTIVE);
            assertThat(session.all(SafetyNote.class)).extracting(SafetyNote::getText)
                    .contains("Benzoil peroksid povučen - poznata alergija ili unakrsna reaktivnost");
            assertThat(session.timesFired("Benzoil peroksid i retinoid u istoj rutini")).isZero();
        }
    }

    @Test
    void currentProductWithAllergenGivesWarning() {
        Questionnaires questionnaire = Questionnaires.standard();
        questionnaire.lifestyle().setAllergies(List.of("methylisothiazolinone"));
        questionnaire.lifestyle().setCurrentProducts(List.of(new Product("tečni sapun",
                List.of(new ProductIngredient("aqua", null), new ProductIngredient("kathon cg", null)), Set.of())));

        try (AnalysisSession session = new AnalysisSession()) {
            session.insertAll(questionnaire.facts()).fire();

            assertThat(session.all(SafetyNote.class)).extracting(SafetyNote::getText).contains(
                    "Proizvod tečni sapun sadrži kathon cg, na koji ste alergični. Prestanite da ga koristite.",
                    "Proizvod tečni sapun sadrži methylisothiazolinone, na koji ste alergični. Prestanite da ga koristite.");
            assertThat(session.all(SafetyNote.class)).doesNotHaveDuplicates();
        }
    }
}
