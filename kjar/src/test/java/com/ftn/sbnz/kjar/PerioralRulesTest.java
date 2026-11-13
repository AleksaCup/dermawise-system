package com.ftn.sbnz.kjar;

import com.ftn.sbnz.model.enums.Condition;
import com.ftn.sbnz.model.facts.Diagnosis;
import com.ftn.sbnz.model.questionnaire.RosaceaAnswers;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PerioralRulesTest {

    static Questionnaires perioral() {
        Questionnaires questionnaire = Questionnaires.standard();
        RosaceaAnswers answers = questionnaire.rosacea();
        answers.setPerioralDistribution(true);
        answers.setPapuleCount(6);
        answers.setRecentTopicalSteroid(true);
        return questionnaire;
    }

    @Test
    void papulesAroundMouthAfterSteroidArePerioralDermatitis() {
        assertThat(hasPerioral(perioral())).isTrue();
    }

    @Test
    void noPerioralDermatitisWithoutSteroid() {
        Questionnaires questionnaire = perioral();
        questionnaire.rosacea().setRecentTopicalSteroid(false);

        assertThat(hasPerioral(questionnaire)).isFalse();
    }

    @Test
    void telangiectasiaPointsAwayFromPerioralDermatitis() {
        Questionnaires questionnaire = perioral();
        questionnaire.rosacea().setTelangiectasia(true);

        assertThat(hasPerioral(questionnaire)).isFalse();
    }

    @Test
    void noPerioralDermatitisWithoutPapules() {
        Questionnaires questionnaire = perioral();
        questionnaire.rosacea().setPapuleCount(0);

        assertThat(hasPerioral(questionnaire)).isFalse();
    }

    static boolean hasPerioral(Questionnaires questionnaire) {
        try (AnalysisSession session = new AnalysisSession()) {
            session.insertAll(questionnaire.facts()).fire();
            return session.all(Diagnosis.class).stream()
                    .anyMatch(d -> d.getCondition() == Condition.PERIORAL_DERMATITIS);
        }
    }
}
