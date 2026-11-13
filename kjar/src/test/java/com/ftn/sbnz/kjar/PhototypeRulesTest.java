package com.ftn.sbnz.kjar;

import com.ftn.sbnz.model.enums.EthnicOrigin;
import com.ftn.sbnz.model.enums.EyeColor;
import com.ftn.sbnz.model.enums.FaceRegion;
import com.ftn.sbnz.model.enums.HairColor;
import com.ftn.sbnz.model.enums.SkinColor;
import com.ftn.sbnz.model.enums.SunReaction;
import com.ftn.sbnz.model.enums.TanTendency;
import com.ftn.sbnz.model.facts.DataReady;
import com.ftn.sbnz.model.facts.SafetyNote;
import com.ftn.sbnz.model.facts.User;
import com.ftn.sbnz.model.questionnaire.PhototypeAnswers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class PhototypeRulesTest {

    @Test
    void completeQuestionnaireIsReady() {
        try (AnalysisSession session = new AnalysisSession()) {
            session.insertAll(Questionnaires.standard().facts()).fire();

            assertThat(session.all(DataReady.class)).hasSize(1);
            assertThat(session.timesFired("Upitnik spreman")).isEqualTo(1);
        }
    }

    @Test
    void missingSectionIsNotReady() {
        try (AnalysisSession session = new AnalysisSession()) {
            session.insertAll(Questionnaires.standard().withoutScorad().facts()).fire();

            assertThat(session.all(DataReady.class)).isEmpty();
        }
    }

    @Test
    void missingRegionIsNotReady() {
        try (AnalysisSession session = new AnalysisSession()) {
            session.insertAll(Questionnaires.standard().withoutRegion(FaceRegion.CHIN).facts()).fire();

            assertThat(session.all(DataReady.class)).isEmpty();
        }
    }

    @ParameterizedTest(name = "{0}, {1}, {2} -> tip {3}, MED {4}")
    @CsvSource({
            "ALWAYS_BURNS, NEVER, VERY_FAIR, 1, 20",
            "ALWAYS_BURNS, MINIMAL, FAIR, 2, 30",
            "BURNS_THEN_TANS, GRADUAL, LIGHT_BROWN, 3, 40",
            "RARELY_BURNS, EASILY, OLIVE, 4, 50",
            "NEVER_BURNS, DEEPLY, OLIVE, 5, 75",
            "NEVER_BURNS, DEEPLY, DARK_BROWN, 6, 120",
            "NEVER_BURNS, DEEPLY, BLACK, 6, 120"
    })
    void phototypeFromBurnAndTanAnswers(SunReaction reaction, TanTendency tan, SkinColor skin,
                                        int expectedType, double expectedMed) {
        try (AnalysisSession session = new AnalysisSession()) {
            Questionnaires questionnaire = Questionnaires.standard().sun(reaction, tan, skin);
            session.insertAll(questionnaire.facts()).fire();

            User user = questionnaire.user();
            assertThat(user.getFototip()).isEqualTo(expectedType);
            assertThat(user.getMed()).isEqualTo(expectedMed);
        }
    }

    @Test
    void phototypeIsNotSetWithoutCompleteQuestionnaire() {
        try (AnalysisSession session = new AnalysisSession()) {
            Questionnaires questionnaire = Questionnaires.standard().withoutScorad();
            session.insertAll(questionnaire.facts()).fire();

            assertThat(questionnaire.user().getFototip()).isZero();
            assertThat(questionnaire.user().getMed()).isZero();
        }
    }

    @Test
    void lightGeneticProfileWithTypeFiveGivesOneWarning() {
        try (AnalysisSession session = new AnalysisSession()) {
            Questionnaires questionnaire = Questionnaires.standard().phototype(new PhototypeAnswers(null,
                    SkinColor.VERY_FAIR, EyeColor.BLUE, HairColor.RED, EthnicOrigin.NORTHERN_EUROPEAN,
                    SunReaction.NEVER_BURNS, TanTendency.DEEPLY));
            session.insertAll(questionnaire.facts()).fire();

            assertThat(questionnaire.user().getFototip()).isEqualTo(5);
            assertThat(session.all(SafetyNote.class)).extracting(SafetyNote::getText)
                    .containsExactly("Boja kože, očiju i kose ne odgovara fototipu 5. Proverite odgovor o reakciji na sunce.");
            assertThat(session.timesFired("Svetla predispozicija uz tamni fototip")).isEqualTo(1);
        }
    }

    @Test
    void darkGeneticProfileWithTypeOneGivesWarning() {
        try (AnalysisSession session = new AnalysisSession()) {
            Questionnaires questionnaire = Questionnaires.standard().phototype(new PhototypeAnswers(null,
                    SkinColor.BLACK, EyeColor.DARK_BROWN, HairColor.BLACK, EthnicOrigin.AFRICAN,
                    SunReaction.ALWAYS_BURNS, TanTendency.NEVER));
            session.insertAll(questionnaire.facts()).fire();

            assertThat(questionnaire.user().getFototip()).isEqualTo(1);
            assertThat(session.timesFired("Tamna predispozicija uz svetli fototip")).isEqualTo(1);
        }
    }

    @Test
    void consistentAnswersGiveNoWarning() {
        try (AnalysisSession session = new AnalysisSession()) {
            Questionnaires questionnaire = Questionnaires.standard().phototype(new PhototypeAnswers(null,
                    SkinColor.VERY_FAIR, EyeColor.BLUE, HairColor.RED, EthnicOrigin.NORTHERN_EUROPEAN,
                    SunReaction.ALWAYS_BURNS, TanTendency.NEVER));
            session.insertAll(questionnaire.facts()).fire();

            assertThat(questionnaire.user().getFototip()).isEqualTo(1);
            assertThat(session.all(SafetyNote.class)).isEmpty();
        }
    }

    @Test
    void twoLightMarkersAreNotEnoughForWarning() {
        try (AnalysisSession session = new AnalysisSession()) {
            Questionnaires questionnaire = Questionnaires.standard().phototype(new PhototypeAnswers(null,
                    SkinColor.OLIVE, EyeColor.BLUE, HairColor.BLONDE, EthnicOrigin.SOUTHERN_EUROPEAN,
                    SunReaction.NEVER_BURNS, TanTendency.DEEPLY));
            session.insertAll(questionnaire.facts()).fire();

            assertThat(session.all(SafetyNote.class)).isEmpty();
        }
    }

    @Test
    void eachPhototypeRuleFiresOnce() {
        try (AnalysisSession session = new AnalysisSession()) {
            session.insertAll(Questionnaires.standard().facts()).fire();

            assertThat(session.timesFired("Upitnik spreman")).isEqualTo(1);
            assertThat(session.timesFired("Fototip III")).isEqualTo(1);
            assertThat(session.timesFired("MED za fototip")).isEqualTo(1);
        }
    }
}
