package com.ftn.sbnz.kjar;

import com.ftn.sbnz.model.enums.FaceRegion;
import com.ftn.sbnz.model.enums.LesionType;
import com.ftn.sbnz.model.facts.Nalaz;
import com.ftn.sbnz.model.facts.NalazCode;
import com.ftn.sbnz.model.questionnaire.RosaceaAnswers;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FindingRulesTest {

    @Test
    void noFindingsWithoutSigns() {
        assertThat(findings(Questionnaires.standard())).isEmpty();
    }

    @Test
    void comedonesInAnyRegionGiveOneFinding() {
        Questionnaires questionnaire = Questionnaires.standard()
                .lesion(FaceRegion.NOSE, LesionType.COMEDONES)
                .lesion(FaceRegion.CHIN, LesionType.COMEDONES);

        assertThat(findings(questionnaire)).containsExactly(NalazCode.KOMEDONI);
    }

    @Test
    void rosaceaAnswersGiveMatchingFindings() {
        Questionnaires questionnaire = Questionnaires.standard();
        RosaceaAnswers rosacea = questionnaire.rosacea();
        rosacea.setPersistentErythema(true);
        rosacea.setCentrofacialDistribution(true);
        rosacea.setTelangiectasia(true);
        rosacea.setPapulesPustulesWithoutComedones(true);
        rosacea.setFlushingPerWeek(3);
        rosacea.setOcularSymptoms(true);
        rosacea.setPhymatousChanges(true);

        assertThat(findings(questionnaire)).containsExactlyInAnyOrder(
                NalazCode.PERZISTENTNI_ERITEM,
                NalazCode.CENTROFACIJALNA_DISTRIBUCIJA,
                NalazCode.TELANGIEKTAZIJE,
                NalazCode.PAPULE_PUSTULE_BEZ_KOMEDONA,
                NalazCode.FLUSHING_UPITNIK,
                NalazCode.OKULARNI_SIMPTOMI,
                NalazCode.FIMATOZNE_PROMENE);
    }

    @Test
    void flushingOncePerWeekIsNotAFinding() {
        Questionnaires questionnaire = Questionnaires.standard();
        questionnaire.rosacea().setFlushingPerWeek(1);

        assertThat(findings(questionnaire)).isEmpty();
    }

    @Test
    void perioralAnswersGiveFindings() {
        Questionnaires questionnaire = Questionnaires.standard();
        questionnaire.rosacea().setPerioralDistribution(true);
        questionnaire.rosacea().setPapuleCount(4);
        questionnaire.rosacea().setRecentTopicalSteroid(true);

        assertThat(findings(questionnaire)).containsExactlyInAnyOrder(
                NalazCode.PERIORALNA_DISTRIBUCIJA,
                NalazCode.PERIORALNE_PAPULE,
                NalazCode.SKORASNJI_STEROID);
    }

    static List<String> findings(Questionnaires questionnaire) {
        try (AnalysisSession session = new AnalysisSession()) {
            session.insertAll(questionnaire.facts()).fire();
            return session.all(Nalaz.class).stream().map(Nalaz::getCode).toList();
        }
    }
}
