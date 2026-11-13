package com.ftn.sbnz.kjar;

import com.ftn.sbnz.model.enums.BodyArea;
import com.ftn.sbnz.model.enums.Condition;
import com.ftn.sbnz.model.enums.FaceRegion;
import com.ftn.sbnz.model.enums.LesionType;
import com.ftn.sbnz.model.enums.RecommendationStatus;
import com.ftn.sbnz.model.enums.RoutineSlot;
import com.ftn.sbnz.model.enums.RoutineStep;
import com.ftn.sbnz.model.enums.SkinColor;
import com.ftn.sbnz.model.enums.SunReaction;
import com.ftn.sbnz.model.enums.SymptomType;
import com.ftn.sbnz.model.enums.TanTendency;
import com.ftn.sbnz.model.facts.Diagnosis;
import com.ftn.sbnz.model.facts.Recommendation;
import com.ftn.sbnz.model.facts.Referral;
import com.ftn.sbnz.model.facts.SafetyNote;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

class TherapyRulesTest {

    @Test
    void fairSkinGetsSpf50() {
        Questionnaires questionnaire = Questionnaires.standard()
                .sun(SunReaction.ALWAYS_BURNS, TanTendency.MINIMAL, SkinColor.FAIR);

        assertThat(spf(questionnaire)).containsExactly("SPF 50");
    }

    @Test
    void typeThreeGetsSpf30() {
        assertThat(spf(Questionnaires.standard())).containsExactly("SPF 30");
    }

    @Test
    void longSunExposureGetsSpf50WithReapplying() {
        Questionnaires questionnaire = Questionnaires.standard();
        questionnaire.lifestyle().setDailySunMinutes(120);

        List<Recommendation> spf = analyse(questionnaire).stream()
                .filter(r -> r.getStep() == RoutineStep.SPF).toList();

        assertThat(spf).singleElement().satisfies(r -> {
            assertThat(r.getSastojak()).isEqualTo("SPF 50");
            assertThat(r.getFrequency()).contains("2 sata");
        });
    }

    @Test
    void baseRoutineHasCleansingAndHydrationMorningAndEvening() {
        List<Recommendation> routine = analyse(Questionnaires.standard());

        assertThat(routine).filteredOn(r -> r.getStep() == RoutineStep.CLEANSING)
                .extracting(Recommendation::getSlot).containsExactlyInAnyOrder(RoutineSlot.MORNING, RoutineSlot.EVENING);
        assertThat(routine).filteredOn(r -> r.getStep() == RoutineStep.HYDRATION)
                .extracting(Recommendation::getSastojak).containsOnly("krema za hidrataciju");
    }

    @Test
    void recommendationKeepsRuleNameAndSource() {
        Recommendation spf = analyse(Questionnaires.standard()).stream()
                .filter(r -> r.getStep() == RoutineStep.SPF).findFirst().orElseThrow();

        assertThat(spf.getRuleName()).isEqualTo("SPF 30 za fototip III-VI");
        assertThat(spf.getSource()).isEqualTo("Fitzpatrick 1975");
        assertThat(spf.getStatus()).isEqualTo(RecommendationStatus.ACTIVE);
    }

    static Questionnaires moderateAcne() {
        return SeverityScoreTest.proposalExample().lesion(FaceRegion.CHEST_BACK, LesionType.PAPULES)
                .sun(SunReaction.ALWAYS_BURNS, TanTendency.MINIMAL, SkinColor.FAIR);
    }

    @Test
    void mildAcneOnLightSkinGetsAdapalene() {
        List<Recommendation> treatment = treatment(SeverityScoreTest.proposalExample());

        assertThat(treatment).singleElement().satisfies(r -> {
            assertThat(r.getSastojak()).isEqualTo("adapalen");
            assertThat(r.getKoncentracija()).isEqualTo(0.1);
            assertThat(r.getSlot()).isEqualTo(RoutineSlot.EVENING);
            assertThat(r.getStatus()).isEqualTo(RecommendationStatus.ACTIVE);
        });
    }

    @Test
    void mildAcneOnDarkSkinGetsAzelaicAcidForReview() {
        Questionnaires questionnaire = SeverityScoreTest.proposalExample()
                .sun(SunReaction.RARELY_BURNS, TanTendency.EASILY, SkinColor.OLIVE);

        List<Recommendation> treatment = treatment(questionnaire);

        assertThat(treatment).extracting(Recommendation::getSastojak).containsOnly("azelainska kiselina");
        assertThat(treatment).extracting(Recommendation::getSlot)
                .containsExactlyInAnyOrder(RoutineSlot.MORNING, RoutineSlot.EVENING);
        assertThat(treatment).extracting(Recommendation::getStatus).containsOnly(RecommendationStatus.PENDING_REVIEW);
    }

    @Test
    void moderateAcneOnTypeTwoGetsAdapaleneAndBenzoylPeroxide() {
        List<Recommendation> treatment = treatment(moderateAcne());

        assertThat(treatment).extracting(Recommendation::getSastojak, Recommendation::getKoncentracija)
                .containsExactlyInAnyOrder(
                        tuple("adapalen", 0.1),
                        tuple("benzoil peroksid", 2.5));
    }

    @Test
    void moderateAcneOnTypeFiveGetsAzelaicAcid() {
        Questionnaires questionnaire = SeverityScoreTest.proposalExample().lesion(FaceRegion.CHEST_BACK, LesionType.PAPULES)
                .sun(SunReaction.NEVER_BURNS, TanTendency.DEEPLY, SkinColor.OLIVE);

        assertThat(treatment(questionnaire)).extracting(Recommendation::getSastojak).containsOnly("azelainska kiselina");
    }

    @Test
    void severeAcneGetsBenzoylPeroxideWash() {
        Questionnaires questionnaire = Questionnaires.standard();
        for (FaceRegion region : FaceRegion.values()) {
            questionnaire.lesion(region, LesionType.NODULES);
        }

        assertThat(treatment(questionnaire)).singleElement()
                .extracting(Recommendation::getForm).isEqualTo("gel za umivanje");
    }

    @Test
    void acneHydrationIsNonComedogenic() {
        List<Recommendation> hydration = analyse(moderateAcne()).stream()
                .filter(r -> r.getStep() == RoutineStep.HYDRATION).toList();

        assertThat(hydration).extracting(Recommendation::getSastojak).containsOnly("nekomedogena krema za hidrataciju");
        assertThat(hydration).hasSize(2);
    }

    @Test
    void moderatePprGetsIvermectinAndMetronidazoleForReview() {
        List<Recommendation> treatment = treatment(RosaceaRulesTest.pprModerate());

        assertThat(treatment).extracting(Recommendation::getSastojak, Recommendation::getSlot)
                .containsExactlyInAnyOrder(
                        tuple("ivermektin", RoutineSlot.EVENING),
                        tuple("metronidazol", RoutineSlot.MORNING));
        assertThat(treatment).extracting(Recommendation::getStatus).containsOnly(RecommendationStatus.PENDING_REVIEW);
    }

    @Test
    void mildPprGetsAzelaicAcid() {
        Questionnaires questionnaire = RosaceaRulesTest.pprModerate();
        questionnaire.rosacea().setPersistentErythemaGrade(1);
        questionnaire.rosacea().setPapulesPustulesGrade(1);

        assertThat(treatment(questionnaire)).extracting(Recommendation::getSastojak).containsOnly("azelainska kiselina");
    }

    @Test
    void etrGetsBrimonidineAndNiacinamide() {
        List<Recommendation> treatment = treatment(RosaceaRulesTest.etr());

        assertThat(treatment).extracting(Recommendation::getSastojak, Recommendation::isRequiresPrescription)
                .containsExactlyInAnyOrder(tuple("brimonidin", true), tuple("niacinamid", false));
    }

    @Test
    void rosaceaHydrationIsForSensitiveSkin() {
        List<Recommendation> hydration = analyse(RosaceaRulesTest.pprModerate()).stream()
                .filter(r -> r.getStep() == RoutineStep.HYDRATION).toList();

        assertThat(hydration).extracting(Recommendation::getSastojak).containsOnly("krema za osetljivu kožu bez mirisa");
    }

    @Test
    void perioralDermatitisStopsSteroidAndGetsMetronidazole() {
        try (AnalysisSession session = new AnalysisSession()) {
            session.insertAll(PerioralRulesTest.perioral().facts()).fire();

            assertThat(session.all(Recommendation.class)).filteredOn(r -> r.getStep() == RoutineStep.TREATMENT)
                    .extracting(Recommendation::getSastojak).containsOnly("metronidazol");
            assertThat(session.all(SafetyNote.class)).extracting(SafetyNote::getText).asString()
                    .contains("Prekinuti topijski kortikosteroid");
        }
    }

    static Questionnaires atopic(double area, int intensity, int pruritus) {
        Questionnaires questionnaire = Questionnaires.standard();
        questionnaire.scorad().setAffectedAreaPercent(area);
        questionnaire.scorad().setErythema(Math.min(intensity, 3));
        questionnaire.scorad().setDryness(Math.max(intensity - 3, 0));
        questionnaire.scorad().setPruritus(pruritus);
        return questionnaire;
    }

    @Test
    void mildAtopicDermatitisGetsOnlyEmollient() {
        List<Recommendation> routine = analyse(atopic(20, 2, 3));

        assertThat(routine).filteredOn(r -> r.getStep() == RoutineStep.HYDRATION)
                .extracting(Recommendation::getSastojak).containsOnly("emolijens bez mirisa");
        assertThat(routine).filteredOn(r -> r.getStep() == RoutineStep.TREATMENT).isEmpty();
    }

    @Test
    void moderateAtopicDermatitisGetsHydrocortisoneForSevenDays() {
        List<Recommendation> treatment = treatment(atopic(50, 4, 8));

        assertThat(treatment).singleElement().satisfies(r -> {
            assertThat(r.getSastojak()).isEqualTo("hidrokortizon");
            assertThat(r.getFrequency()).contains("7 dana");
        });
    }

    @Test
    void seborrheicDermatitisGetsZincPyrithione() {
        Questionnaires questionnaire = Questionnaires.standard()
                .symptom(SymptomType.SCALING, BodyArea.SCALP)
                .symptom(SymptomType.ERYTHEMA, BodyArea.EYEBROWS);

        assertThat(analyse(questionnaire)).extracting(Recommendation::getSastojak).contains("cink pirition");
    }

    @Test
    void contactDermatitisStopsProductAndGetsHydrocortisone() {
        try (AnalysisSession session = new AnalysisSession()) {
            session.insertAll(Questionnaires.standard().facts());
            session.insert(new Diagnosis(Questionnaires.USER_ID, Condition.CONTACT_DERMATITIS, null, null, null));
            session.fire();

            assertThat(session.all(Recommendation.class)).extracting(Recommendation::getSastojak)
                    .contains("hidrokortizon", "emolijens bez mirisa")
                    .doesNotContain("krema za hidrataciju");
            assertThat(session.all(SafetyNote.class)).extracting(SafetyNote::getText)
                    .contains("Prekinuti proizvod koji je izazvao reakciju.");
        }
    }

    @Test
    void atopicAndContactDermatitisShareOneEmollientAndOneHydrocortisone() {
        try (AnalysisSession session = new AnalysisSession()) {
            session.insertAll(atopic(50, 4, 8).facts());
            session.insert(new Diagnosis(Questionnaires.USER_ID, Condition.CONTACT_DERMATITIS, null, null, null));
            session.fire();

            List<Recommendation> all = session.all(Recommendation.class);
            assertThat(all).filteredOn(r -> r.getSastojak().equals("hidrokortizon")).hasSize(1);
            assertThat(all).filteredOn(r -> r.getSastojak().equals("emolijens bez mirisa")).hasSize(2);
        }
    }

    @Test
    void severeAcneIsReferredToDermatologist() {
        Questionnaires questionnaire = Questionnaires.standard();
        for (FaceRegion region : FaceRegion.values()) {
            questionnaire.lesion(region, LesionType.PUSTULES);
        }

        assertThat(referrals(questionnaire)).singleElement().satisfies(r -> {
            assertThat(r.getForCondition()).isEqualTo(Condition.ACNE);
            assertThat(r.getSpecialist()).isEqualTo("dermatolog");
        });
    }

    @Test
    void ocularRosaceaIsReferredToDermatologistAndOphthalmologist() {
        Questionnaires questionnaire = RosaceaRulesTest.etr();
        questionnaire.rosacea().setOcularSymptoms(true);

        assertThat(referrals(questionnaire)).extracting(Referral::getSpecialist)
                .containsExactlyInAnyOrder("dermatolog", "oftalmolog");
    }

    @Test
    void ocularAndPhymatousRosaceaGiveOneDermatologistReferral() {
        Questionnaires questionnaire = RosaceaRulesTest.etr();
        questionnaire.rosacea().setOcularSymptoms(true);
        questionnaire.rosacea().setPhymatousChanges(true);

        assertThat(referrals(questionnaire)).extracting(Referral::getSpecialist)
                .containsExactlyInAnyOrder("dermatolog", "oftalmolog");
    }

    @Test
    void severePprIsReferred() {
        Questionnaires questionnaire = RosaceaRulesTest.pprModerate();
        questionnaire.rosacea().setPapulesPustulesGrade(3);

        assertThat(referrals(questionnaire)).extracting(Referral::getForCondition).containsExactly(Condition.ROSACEA);
        assertThat(treatment(questionnaire)).isEmpty();
    }

    @Test
    void severeAtopicDermatitisIsReferred() {
        assertThat(referrals(atopic(100, 6, 10))).extracting(Referral::getForCondition)
                .containsExactly(Condition.ATOPIC_DERMATITIS);
    }

    @Test
    void mildCasesAreNotReferred() {
        assertThat(referrals(SeverityScoreTest.proposalExample())).isEmpty();
        assertThat(referrals(RosaceaRulesTest.pprModerate())).isEmpty();
    }

    @Test
    void noRuleFiresTwiceInBusyCase() {
        Questionnaires questionnaire = RosaceaRulesTest.pprModerate()
                .lesion(FaceRegion.NOSE, LesionType.COMEDONES)
                .symptom(SymptomType.SCALING, BodyArea.SCALP)
                .symptom(SymptomType.ERYTHEMA, BodyArea.EYEBROWS);
        questionnaire.rosacea().setTelangiectasia(true);
        questionnaire.rosacea().setOcularSymptoms(true);
        questionnaire.rosacea().setPhymatousChanges(true);
        questionnaire.scorad().setAffectedAreaPercent(40);
        questionnaire.scorad().setErythema(2);
        questionnaire.scorad().setPruritus(6);
        questionnaire.lifestyle().setDailySunMinutes(90);

        try (AnalysisSession session = new AnalysisSession()) {
            session.insertAll(questionnaire.facts()).fire();

            assertThat(session.firedRules()).doesNotHaveDuplicates();
        }
    }

    private List<Referral> referrals(Questionnaires questionnaire) {
        try (AnalysisSession session = new AnalysisSession()) {
            session.insertAll(questionnaire.facts()).fire();
            return session.all(Referral.class);
        }
    }

    static List<Recommendation> treatment(Questionnaires questionnaire) {
        return analyse(questionnaire).stream().filter(r -> r.getStep() == RoutineStep.TREATMENT).toList();
    }

    static List<Recommendation> analyse(Questionnaires questionnaire) {
        try (AnalysisSession session = new AnalysisSession()) {
            session.insertAll(questionnaire.facts()).fire();
            return session.all(Recommendation.class);
        }
    }

    private List<String> spf(Questionnaires questionnaire) {
        return analyse(questionnaire).stream()
                .filter(r -> r.getStep() == RoutineStep.SPF)
                .map(Recommendation::getSastojak)
                .toList();
    }
}
