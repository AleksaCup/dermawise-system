package com.ftn.sbnz.kjar;

import com.ftn.sbnz.model.enums.Condition;
import com.ftn.sbnz.model.enums.FaceRegion;
import com.ftn.sbnz.model.enums.LesionType;
import com.ftn.sbnz.model.enums.RosaceaSubtype;
import com.ftn.sbnz.model.enums.Severity;
import com.ftn.sbnz.model.facts.Diagnosis;
import com.ftn.sbnz.model.facts.RosaceaPrimaryCriterion;
import com.ftn.sbnz.model.questionnaire.RosaceaAnswers;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RosaceaRulesTest {

    static Questionnaires pprModerate() {
        Questionnaires questionnaire = Questionnaires.standard();
        RosaceaAnswers rosacea = questionnaire.rosacea();
        rosacea.setPersistentErythema(true);
        rosacea.setCentrofacialDistribution(true);
        rosacea.setFlushingPerWeek(3);
        rosacea.setPapulesPustulesWithoutComedones(true);
        rosacea.setPapuleCount(8);
        rosacea.setPustuleCount(2);
        rosacea.setFlushingGrade(1);
        rosacea.setPersistentErythemaGrade(2);
        rosacea.setPapulesPustulesGrade(2);
        questionnaire.lesion(FaceRegion.LEFT_CHEEK, LesionType.PAPULES)
                .lesion(FaceRegion.RIGHT_CHEEK, LesionType.PUSTULES);
        return questionnaire;
    }

    static Questionnaires etr() {
        Questionnaires questionnaire = Questionnaires.standard();
        RosaceaAnswers rosacea = questionnaire.rosacea();
        rosacea.setPersistentErythema(true);
        rosacea.setCentrofacialDistribution(true);
        rosacea.setTelangiectasia(true);
        rosacea.setPapuleCount(1);
        rosacea.setPersistentErythemaGrade(1);
        rosacea.setTelangiectasiaGrade(1);
        return questionnaire;
    }

    @Test
    void pprWithModerateSigns() {
        List<Diagnosis> rosacea = rosacea(pprModerate());

        assertThat(rosacea).singleElement().satisfies(d -> {
            assertThat(d.getSubtype()).isEqualTo(RosaceaSubtype.PPR);
            assertThat(d.getSeverity()).isEqualTo(Severity.MODERATE);
        });
    }

    @Test
    void threePustulesAreEnoughForPpr() {
        Questionnaires questionnaire = pprModerate();
        questionnaire.rosacea().setPapuleCount(0);
        questionnaire.rosacea().setPustuleCount(3);

        assertThat(rosacea(questionnaire)).extracting(Diagnosis::getSubtype).contains(RosaceaSubtype.PPR);
    }

    @Test
    void comedonesPreventPpr() {
        Questionnaires questionnaire = pprModerate().lesion(FaceRegion.NOSE, LesionType.COMEDONES);

        assertThat(rosacea(questionnaire)).extracting(Diagnosis::getSubtype).doesNotContain(RosaceaSubtype.PPR);
    }

    @Test
    void etrWithTelangiectasia() {
        List<Diagnosis> rosacea = rosacea(etr());

        assertThat(rosacea).singleElement().satisfies(d -> {
            assertThat(d.getSubtype()).isEqualTo(RosaceaSubtype.ETR);
            assertThat(d.getSeverity()).isEqualTo(Severity.MILD);
        });
    }

    @Test
    void etrFromFlushingWithoutTelangiectasia() {
        Questionnaires questionnaire = Questionnaires.standard();
        questionnaire.rosacea().setPersistentErythema(true);
        questionnaire.rosacea().setCentrofacialDistribution(true);
        questionnaire.rosacea().setFlushingPerWeek(2);
        questionnaire.rosacea().setFlushingGrade(3);

        List<Diagnosis> rosacea = rosacea(questionnaire);

        assertThat(rosacea).singleElement().satisfies(d -> {
            assertThat(d.getSubtype()).isEqualTo(RosaceaSubtype.ETR);
            assertThat(d.getSeverity()).isEqualTo(Severity.SEVERE);
        });
    }

    @Test
    void telangiectasiaAndFlushingGiveOnlyOneEtr() {
        Questionnaires questionnaire = etr();
        questionnaire.rosacea().setFlushingPerWeek(4);

        assertThat(rosacea(questionnaire)).hasSize(1);
    }

    @Test
    void phymatousAndOcularSubtypes() {
        Questionnaires questionnaire = etr();
        questionnaire.rosacea().setPhymatousChanges(true);
        questionnaire.rosacea().setOcularSymptoms(true);

        assertThat(rosacea(questionnaire)).extracting(Diagnosis::getSubtype)
                .containsExactlyInAnyOrder(RosaceaSubtype.ETR, RosaceaSubtype.PHYMATOUS, RosaceaSubtype.OCULAR);
    }

    @Test
    void noCriterionWithoutCentrofacialDistribution() {
        Questionnaires questionnaire = etr();
        questionnaire.rosacea().setCentrofacialDistribution(false);

        try (AnalysisSession session = new AnalysisSession()) {
            session.insertAll(questionnaire.facts()).fire();

            assertThat(session.all(RosaceaPrimaryCriterion.class)).isEmpty();
            assertThat(session.all(Diagnosis.class)).isEmpty();
        }
    }

    static List<Diagnosis> rosacea(Questionnaires questionnaire) {
        try (AnalysisSession session = new AnalysisSession()) {
            session.insertAll(questionnaire.facts()).fire();
            assertThat(session.timesFired("Primarni kriterijum rozaceje")).isLessThanOrEqualTo(1);
            return session.all(Diagnosis.class).stream()
                    .filter(d -> d.getCondition() == Condition.ROSACEA)
                    .toList();
        }
    }
}
