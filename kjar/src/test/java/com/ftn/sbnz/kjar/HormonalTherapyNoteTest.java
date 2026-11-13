package com.ftn.sbnz.kjar;

import com.ftn.sbnz.model.enums.FaceRegion;
import com.ftn.sbnz.model.enums.LesionType;
import com.ftn.sbnz.model.facts.SafetyNote;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class HormonalTherapyNoteTest {

    @Test
    void moderateAcneWithHormonalTherapyGivesNote() {
        Questionnaires questionnaire = SeverityScoreTest.proposalExample().lesion(FaceRegion.CHEST_BACK, LesionType.PAPULES);
        questionnaire.lifestyle().setHormonalTherapy(true);

        assertThat(notes(questionnaire)).singleElement()
                .extracting(SafetyNote::getText).asString()
                .contains("hormonska terapija");
    }

    @Test
    void mildAcneWithHormonalTherapyGivesNoNote() {
        Questionnaires questionnaire = SeverityScoreTest.proposalExample();
        questionnaire.lifestyle().setHormonalTherapy(true);

        assertThat(notes(questionnaire)).isEmpty();
    }

    @Test
    void moderateAcneWithoutHormonalTherapyGivesNoNote() {
        Questionnaires questionnaire = SeverityScoreTest.proposalExample().lesion(FaceRegion.CHEST_BACK, LesionType.PAPULES);

        assertThat(notes(questionnaire)).isEmpty();
    }

    private List<SafetyNote> notes(Questionnaires questionnaire) {
        try (AnalysisSession session = new AnalysisSession()) {
            session.insertAll(questionnaire.facts()).fire();
            return session.all(SafetyNote.class).stream()
                    .filter(n -> n.getText().contains("hormonska terapija"))
                    .toList();
        }
    }
}
