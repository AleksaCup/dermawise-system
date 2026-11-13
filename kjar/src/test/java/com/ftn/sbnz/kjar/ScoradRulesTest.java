package com.ftn.sbnz.kjar;

import com.ftn.sbnz.model.enums.Condition;
import com.ftn.sbnz.model.enums.Severity;
import com.ftn.sbnz.model.facts.Diagnosis;
import com.ftn.sbnz.model.facts.SeverityScore;
import com.ftn.sbnz.model.questionnaire.ScoradAnswers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class ScoradRulesTest {

    @Test
    void formulaCombinesAreaIntensityAndSymptoms() {
        Result result = analyse(30, 2, 1, 1, 0, 0, 2, 5, 3);

        assertThat(result.score()).isEqualTo(35.0);
        assertThat(result.diagnosis()).map(Diagnosis::getSeverity).contains(Severity.MODERATE);
    }

    @Test
    void maximumScoreIs103AndSevere() {
        Result result = analyse(100, 3, 3, 3, 3, 3, 3, 10, 10);

        assertThat(result.score()).isEqualTo(103.0);
        assertThat(result.diagnosis()).map(Diagnosis::getSeverity).contains(Severity.SEVERE);
    }

    @Test
    void zeroScoreGivesNoAtopicDermatitis() {
        Result result = analyse(0, 0, 0, 0, 0, 0, 0, 0, 0);

        assertThat(result.score()).isZero();
        assertThat(result.diagnosis()).isEmpty();
    }

    @ParameterizedTest(name = "A={0}, B={1}, C={2} -> {3} {4}")
    @CsvSource({
            "49.5, 2, 8, 24.9, MILD",
            "50, 2, 8, 25.0, MODERATE",
            "100, 6, 9, 50.0, MODERATE",
            "95.5, 6, 10, 50.1, SEVERE"
    })
    void categoryBoundaries(double area, int intensity, int pruritus, double expectedScore, Severity expected) {
        int[] signs = new int[6];
        for (int i = 0; i < intensity; i++) {
            signs[i % 6]++;
        }
        Result result = analyse(area, signs[0], signs[1], signs[2], signs[3], signs[4], signs[5], pruritus, 0);

        assertThat(result.score()).isEqualTo(expectedScore);
        assertThat(result.diagnosis()).map(Diagnosis::getSeverity).contains(expected);
    }

    private Result analyse(double area, int erythema, int edema, int excoriation, int lichenification,
                           int oozing, int dryness, int pruritus, int sleeplessness) {
        Questionnaires questionnaire = Questionnaires.standard();
        ScoradAnswers scorad = questionnaire.scorad();
        scorad.setAffectedAreaPercent(area);
        scorad.setErythema(erythema);
        scorad.setEdemaPapulation(edema);
        scorad.setExcoriation(excoriation);
        scorad.setLichenification(lichenification);
        scorad.setOozingCrusting(oozing);
        scorad.setDryness(dryness);
        scorad.setPruritus(pruritus);
        scorad.setSleeplessness(sleeplessness);

        try (AnalysisSession session = new AnalysisSession()) {
            session.insertAll(questionnaire.facts()).fire();
            double score = session.all(SeverityScore.class).stream()
                    .filter(s -> s.getScale().equals("SCORAD"))
                    .findFirst().orElseThrow().getValue();
            List<Diagnosis> atopic = session.all(Diagnosis.class).stream()
                    .filter(d -> d.getCondition() == Condition.ATOPIC_DERMATITIS)
                    .toList();
            assertThat(atopic).hasSizeLessThanOrEqualTo(1);
            assertThat(session.timesFired("SCORAD skor")).isEqualTo(1);
            return new Result(score, atopic.stream().findFirst());
        }
    }

    private record Result(double score, Optional<Diagnosis> diagnosis) {
    }
}
