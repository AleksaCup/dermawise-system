package com.ftn.sbnz.kjar;

import com.ftn.sbnz.model.enums.Condition;
import com.ftn.sbnz.model.enums.RecommendationStatus;
import com.ftn.sbnz.model.enums.RosaceaSubtype;
import com.ftn.sbnz.model.enums.RoutineSlot;
import com.ftn.sbnz.model.enums.RoutineStep;
import com.ftn.sbnz.model.enums.Severity;
import com.ftn.sbnz.model.facts.Diagnosis;
import com.ftn.sbnz.model.facts.Recommendation;
import com.ftn.sbnz.model.facts.SafetyNote;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

class FullChainTest {

    @Test
    void fc1ModerateAcneOnPhototypeTwo() {
        try (AnalysisSession session = new AnalysisSession()) {
            session.insertAll(TherapyRulesTest.moderateAcne().facts()).fire();

            assertThat(session.all(Diagnosis.class)).extracting(Diagnosis::getCondition, Diagnosis::getSeverity)
                    .containsExactly(tuple(Condition.ACNE, Severity.MODERATE));
            assertThat(treatment(session)).extracting(Recommendation::getSastojak, Recommendation::getSlot)
                    .containsExactlyInAnyOrder(
                            tuple("adapalen", RoutineSlot.EVENING),
                            tuple("benzoil peroksid", RoutineSlot.MORNING));
            assertThat(session.firedRules()).containsSubsequence(
                    "Upitnik spreman",
                    "Fototip II",
                    "MED za fototip",
                    "GAGS skor",
                    "Kategorija GAGS MODERATE 1",
                    "Akne prema GAGS kategoriji",
                    "Akne umerene, fototip I-III",
                    "Benzoil peroksid i retinoid u istoj rutini");
        }
    }

    @Test
    void fc1SameUserPregnant() {
        Questionnaires questionnaire = TherapyRulesTest.moderateAcne();
        questionnaire.lifestyle().setPregnant(true);

        try (AnalysisSession session = new AnalysisSession()) {
            session.insertAll(questionnaire.facts()).fire();

            List<Recommendation> treatment = treatment(session);
            assertThat(treatment).extracting(Recommendation::getSastojak, Recommendation::getStatus)
                    .containsExactlyInAnyOrder(
                            tuple("adapalen", RecommendationStatus.WITHDRAWN),
                            tuple("benzoil peroksid", RecommendationStatus.ACTIVE),
                            tuple("azelainska kiselina", RecommendationStatus.PENDING_REVIEW));
            assertThat(session.all(SafetyNote.class)).extracting(SafetyNote::getText)
                    .contains("Adapalen povučen - prijavljena trudnoća, zamenjen azelainskom kiselinom 15%");
            assertThat(session.firedRules()).containsSubsequence(
                    "Akne umerene, fototip I-III",
                    "Trudnoća iz upitnika",
                    "Trudnoća: povlačenje retinoida",
                    "Zamena za povučen retinoid");
            assertThat(session.firedRules()).doesNotContain("Benzoil peroksid i retinoid u istoj rutini");
        }
    }

    @Test
    void fc2ModeratePprRosacea() {
        try (AnalysisSession session = new AnalysisSession()) {
            session.insertAll(RosaceaRulesTest.pprModerate().facts()).fire();

            assertThat(session.all(Diagnosis.class)).extracting(Diagnosis::getCondition, Diagnosis::getSubtype,
                            Diagnosis::getSeverity)
                    .containsExactly(tuple(Condition.ROSACEA, RosaceaSubtype.PPR, Severity.MODERATE));
            assertThat(treatment(session)).extracting(Recommendation::getSastojak, Recommendation::getStatus)
                    .containsExactlyInAnyOrder(
                            tuple("ivermektin", RecommendationStatus.PENDING_REVIEW),
                            tuple("metronidazol", RecommendationStatus.PENDING_REVIEW));
            assertThat(session.firedRules()).containsSubsequence(
                    "Nalaz perzistentni eritem",
                    "Primarni kriterijum rozaceje",
                    "Isključene akne: nema komedona uz kriterijum rozaceje",
                    "Rozaceja PPR",
                    "Rozaceja PPR umerena: ivermektin i metronidazol");
        }
    }

    @Test
    void fc2PerioralDermatitis() {
        try (AnalysisSession session = new AnalysisSession()) {
            session.insertAll(PerioralRulesTest.perioral().facts()).fire();

            assertThat(session.all(Diagnosis.class)).extracting(Diagnosis::getCondition)
                    .containsExactly(Condition.PERIORAL_DERMATITIS);
            assertThat(treatment(session)).extracting(Recommendation::getSastojak).containsOnly("metronidazol");
        }
    }

    @Test
    void noRuleFiresTwiceInFullChains() {
        for (Questionnaires questionnaire : List.of(TherapyRulesTest.moderateAcne(), RosaceaRulesTest.pprModerate(),
                PerioralRulesTest.perioral())) {
            try (AnalysisSession session = new AnalysisSession()) {
                session.insertAll(questionnaire.facts()).fire();

                assertThat(session.firedRules()).doesNotHaveDuplicates();
            }
        }
    }

    private List<Recommendation> treatment(AnalysisSession session) {
        return session.all(Recommendation.class).stream().filter(r -> r.getStep() == RoutineStep.TREATMENT).toList();
    }
}
