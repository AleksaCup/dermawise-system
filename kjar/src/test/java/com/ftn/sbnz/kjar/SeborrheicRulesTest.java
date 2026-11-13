package com.ftn.sbnz.kjar;

import com.ftn.sbnz.model.enums.BodyArea;
import com.ftn.sbnz.model.enums.Condition;
import com.ftn.sbnz.model.enums.SymptomType;
import com.ftn.sbnz.model.facts.Diagnosis;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SeborrheicRulesTest {

    @Test
    void scalingAndErythemaInSeborrheicAreas() {
        Questionnaires questionnaire = Questionnaires.standard()
                .symptom(SymptomType.SCALING, BodyArea.SCALP)
                .symptom(SymptomType.ERYTHEMA, BodyArea.NASOLABIAL_FOLDS);

        assertThat(hasSeborrheic(questionnaire)).isTrue();
    }

    @Test
    void repeatedSymptomsGiveOneDiagnosis() {
        Questionnaires questionnaire = Questionnaires.standard()
                .symptom(SymptomType.SCALING, BodyArea.SCALP)
                .symptom(SymptomType.SCALING, BodyArea.EYEBROWS)
                .symptom(SymptomType.ERYTHEMA, BodyArea.BEHIND_EARS)
                .symptom(SymptomType.ERYTHEMA, BodyArea.EYEBROWS);

        try (AnalysisSession session = new AnalysisSession()) {
            session.insertAll(questionnaire.facts()).fire();

            assertThat(session.all(Diagnosis.class)).filteredOn(d -> d.getCondition() == Condition.SEBORRHEIC_DERMATITIS)
                    .hasSize(1);
        }
    }

    @Test
    void scalingOnHandsIsNotSeborrheic() {
        Questionnaires questionnaire = Questionnaires.standard()
                .symptom(SymptomType.SCALING, BodyArea.HANDS)
                .symptom(SymptomType.ERYTHEMA, BodyArea.NASOLABIAL_FOLDS);

        assertThat(hasSeborrheic(questionnaire)).isFalse();
    }

    @Test
    void scalingAloneIsNotEnough() {
        Questionnaires questionnaire = Questionnaires.standard()
                .symptom(SymptomType.SCALING, BodyArea.SCALP)
                .symptom(SymptomType.ITCHING, BodyArea.SCALP);

        assertThat(hasSeborrheic(questionnaire)).isFalse();
    }

    private boolean hasSeborrheic(Questionnaires questionnaire) {
        try (AnalysisSession session = new AnalysisSession()) {
            session.insertAll(questionnaire.facts()).fire();
            return session.all(Diagnosis.class).stream()
                    .anyMatch(d -> d.getCondition() == Condition.SEBORRHEIC_DERMATITIS);
        }
    }
}
