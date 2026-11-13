package com.ftn.sbnz.kjar;

import com.ftn.sbnz.model.enums.Condition;
import com.ftn.sbnz.model.facts.Diagnosis;
import com.ftn.sbnz.model.facts.ExcludedDiagnosis;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ExcludedDiagnosisTest {

    @Test
    void acneIsExcludedForRosaceaWithoutComedones() {
        Result result = analyse(RosaceaRulesTest.pprModerate());

        assertThat(result.diagnoses()).extracting(Diagnosis::getCondition).containsExactly(Condition.ROSACEA);
        assertThat(result.excluded()).filteredOn(e -> e.getCondition() == Condition.ACNE)
                .singleElement()
                .extracting(ExcludedDiagnosis::getReason).asString()
                .startsWith("Nema komedona");
    }

    @Test
    void acneIsExcludedWhenGagsIsZero() {
        Result result = analyse(Questionnaires.standard());

        assertThat(result.excludedConditions()).contains(Condition.ACNE);
    }

    @Test
    void atopicDermatitisIsExcludedWhenScoradIsZero() {
        Result result = analyse(Questionnaires.standard());

        assertThat(result.excludedConditions()).contains(Condition.ATOPIC_DERMATITIS);
    }

    @Test
    void rosaceaIsExcludedWithoutCentrofacialDistribution() {
        Questionnaires questionnaire = RosaceaRulesTest.etr();
        questionnaire.rosacea().setCentrofacialDistribution(false);

        assertThat(analyse(questionnaire).excludedConditions()).contains(Condition.ROSACEA);
    }

    @Test
    void perioralIsExcludedWithoutSteroid() {
        Questionnaires questionnaire = PerioralRulesTest.perioral();
        questionnaire.rosacea().setRecentTopicalSteroid(false);

        assertThat(analyse(questionnaire).excludedConditions()).contains(Condition.PERIORAL_DERMATITIS);
    }

    @Test
    void perioralIsExcludedOnceEvenWithTwoReasons() {
        Questionnaires questionnaire = PerioralRulesTest.perioral();
        questionnaire.rosacea().setRecentTopicalSteroid(false);
        questionnaire.rosacea().setTelangiectasia(true);

        assertThat(analyse(questionnaire).excludedConditions())
                .filteredOn(c -> c == Condition.PERIORAL_DERMATITIS).hasSize(1);
    }

    @Test
    void diagnosedPerioralDermatitisIsNotExcluded() {
        Result result = analyse(PerioralRulesTest.perioral());

        assertThat(result.diagnoses()).extracting(Diagnosis::getCondition)
                .containsExactly(Condition.PERIORAL_DERMATITIS);
        assertThat(result.excludedConditions()).doesNotContain(Condition.PERIORAL_DERMATITIS);
    }

    @Test
    void acneWithComedonesIsNotExcludedByRosacea() {
        Questionnaires questionnaire = SeverityScoreTest.proposalExample();

        Result result = analyse(questionnaire);

        assertThat(result.diagnoses()).extracting(Diagnosis::getCondition).contains(Condition.ACNE);
        assertThat(result.excludedConditions()).doesNotContain(Condition.ACNE);
    }

    private Result analyse(Questionnaires questionnaire) {
        try (AnalysisSession session = new AnalysisSession()) {
            session.insertAll(questionnaire.facts()).fire();
            return new Result(session.all(Diagnosis.class), session.all(ExcludedDiagnosis.class));
        }
    }

    private record Result(List<Diagnosis> diagnoses, List<ExcludedDiagnosis> excluded) {

        List<Condition> excludedConditions() {
            return excluded.stream().map(ExcludedDiagnosis::getCondition).toList();
        }
    }
}
