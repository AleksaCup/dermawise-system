package com.ftn.sbnz.kjar;

import com.ftn.sbnz.model.enums.FaceRegion;
import com.ftn.sbnz.model.enums.LesionType;
import com.ftn.sbnz.model.facts.SeverityScore;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SeverityScoreTest {

    @Test
    void proposalExampleGivesGags17() {
        assertThat(gags(proposalExample())).isEqualTo(17);
    }

    @Test
    void papulesOnChestAndBackGiveGags23() {
        assertThat(gags(proposalExample().lesion(FaceRegion.CHEST_BACK, LesionType.PAPULES))).isEqualTo(23);
    }

    @Test
    void noLesionsGiveGagsZero() {
        assertThat(gags(Questionnaires.standard())).isZero();
    }

    @Test
    void nodulesEverywhereGiveGags44() {
        Questionnaires questionnaire = Questionnaires.standard();
        for (FaceRegion region : FaceRegion.values()) {
            questionnaire.lesion(region, LesionType.NODULES);
        }
        assertThat(gags(questionnaire)).isEqualTo(44);
    }

    static Questionnaires proposalExample() {
        return Questionnaires.standard()
                .lesion(FaceRegion.FOREHEAD, LesionType.PAPULES)
                .lesion(FaceRegion.RIGHT_CHEEK, LesionType.PUSTULES)
                .lesion(FaceRegion.LEFT_CHEEK, LesionType.PAPULES)
                .lesion(FaceRegion.NOSE, LesionType.COMEDONES)
                .lesion(FaceRegion.CHIN, LesionType.PAPULES)
                .lesion(FaceRegion.CHEST_BACK, LesionType.NONE);
    }

    private double gags(Questionnaires questionnaire) {
        try (AnalysisSession session = new AnalysisSession()) {
            session.insertAll(questionnaire.facts()).fire();
            assertThat(session.timesFired("GAGS skor")).isEqualTo(1);
            return session.all(SeverityScore.class).stream()
                    .filter(s -> s.getScale().equals("GAGS"))
                    .findFirst().orElseThrow().getValue();
        }
    }
}
