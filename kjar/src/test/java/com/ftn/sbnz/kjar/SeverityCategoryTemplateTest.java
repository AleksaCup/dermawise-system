package com.ftn.sbnz.kjar;

import com.ftn.sbnz.kjar.template.SeverityCategoryRow;
import com.ftn.sbnz.kjar.template.SeverityCategoryRows;
import com.ftn.sbnz.model.enums.Condition;
import com.ftn.sbnz.model.enums.FaceRegion;
import com.ftn.sbnz.model.enums.LesionType;
import com.ftn.sbnz.model.enums.Severity;
import com.ftn.sbnz.model.facts.Diagnosis;
import com.ftn.sbnz.model.facts.SeverityCategory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SeverityCategoryTemplateTest {

    @Test
    void csvHasFourGagsAndThreeScoradRows() {
        List<SeverityCategoryRow> rows = SeverityCategoryRows.load();

        assertThat(rows).extracting(SeverityCategoryRow::getScale)
                .containsExactly("GAGS", "GAGS", "GAGS", "GAGS", "SCORAD", "SCORAD", "SCORAD");
    }

    @Test
    void templateCreatesOneRulePerRow() {
        String drl = new TemplateCompiler().compile(KnowledgeBaseFactory.SEVERITY_TEMPLATE, SeverityCategoryRows.load());

        assertThat(drl).contains(
                "rule \"Kategorija GAGS MODERATE 1\"",
                "value >= 19, value <= 30",
                "rule \"Kategorija SCORAD SEVERE 6\"");
    }

    @Test
    void proposalExampleWithGags17IsMildAcne() {
        Diagnosis acne = acne(SeverityScoreTest.proposalExample());

        assertThat(acne.getSeverity()).isEqualTo(Severity.MILD);
        assertThat(acne.getScore()).isEqualTo(17);
    }

    @Test
    void gags23IsModerateAcne() {
        Diagnosis acne = acne(SeverityScoreTest.proposalExample().lesion(FaceRegion.CHEST_BACK, LesionType.PAPULES));

        assertThat(acne.getSeverity()).isEqualTo(Severity.MODERATE);
        assertThat(acne.getScore()).isEqualTo(23);
    }

    @Test
    void gagsZeroGivesNoCategoryAndNoAcne() {
        try (AnalysisSession session = new AnalysisSession()) {
            session.insertAll(Questionnaires.standard().facts()).fire();

            assertThat(session.all(SeverityCategory.class)).isEmpty();
            assertThat(session.all(Diagnosis.class)).isEmpty();
        }
    }

    @ParameterizedTest(name = "{0} na svim regionima osim {1} -> {2}")
    @CsvSource({
            "COMEDONES, CHIN, MILD",
            "PAPULES, NOSE, MODERATE",
            "PUSTULES, , SEVERE",
            "NODULES, NOSE, VERY_SEVERE"
    })
    void gagsCategoriesAcrossScale(LesionType lesion, FaceRegion skipped, Severity expected) {
        Questionnaires questionnaire = Questionnaires.standard();
        for (FaceRegion region : FaceRegion.values()) {
            if (region != skipped) {
                questionnaire.lesion(region, lesion);
            }
        }

        assertThat(acne(questionnaire).getSeverity()).isEqualTo(expected);
    }

    @ParameterizedTest(name = "GAGS {0} -> {1}")
    @CsvSource({
            "18, MILD",
            "19, MODERATE",
            "30, MODERATE",
            "31, SEVERE",
            "38, SEVERE",
            "39, VERY_SEVERE"
    })
    void gagsBoundaries(int gags, Severity expected) {
        assertThat(acne(withGags(gags)).getScore()).isEqualTo(gags);
        assertThat(acne(withGags(gags)).getSeverity()).isEqualTo(expected);
    }

    private static Questionnaires withGags(int target) {
        Questionnaires questionnaire = Questionnaires.standard();
        int remaining = target;
        for (FaceRegion region : List.of(FaceRegion.CHEST_BACK, FaceRegion.FOREHEAD, FaceRegion.RIGHT_CHEEK,
                FaceRegion.LEFT_CHEEK, FaceRegion.NOSE, FaceRegion.CHIN)) {
            LesionType chosen = LesionType.NONE;
            for (LesionType lesion : LesionType.values()) {
                if (region.getWeight() * lesion.getGrade() <= remaining) {
                    chosen = lesion;
                }
            }
            questionnaire.lesion(region, chosen);
            remaining -= region.getWeight() * chosen.getGrade();
        }
        assertThat(remaining).as("GAGS %d is reachable", target).isZero();
        return questionnaire;
    }

    private static Diagnosis acne(Questionnaires questionnaire) {
        try (AnalysisSession session = new AnalysisSession()) {
            session.insertAll(questionnaire.facts()).fire();
            List<Diagnosis> acne = session.all(Diagnosis.class).stream()
                    .filter(d -> d.getCondition() == Condition.ACNE)
                    .toList();
            assertThat(acne).hasSize(1);
            return acne.get(0);
        }
    }
}
