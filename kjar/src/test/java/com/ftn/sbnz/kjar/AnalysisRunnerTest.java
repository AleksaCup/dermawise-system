package com.ftn.sbnz.kjar;

import com.ftn.sbnz.kjar.template.MaxConcentrationRows;
import com.ftn.sbnz.model.enums.Condition;
import com.ftn.sbnz.model.enums.RecommendationStatus;
import com.ftn.sbnz.model.enums.RoutineStep;
import com.ftn.sbnz.model.facts.Recommendation;
import com.ftn.sbnz.model.facts.User;
import com.ftn.sbnz.model.result.AnalysisResult;
import org.junit.jupiter.api.Test;
import org.kie.api.KieBase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

class AnalysisRunnerTest {

    private static final KieBase ANALYSIS =
            new KnowledgeBaseFactory().buildWithMaxConcentration(MaxConcentrationRows.load()).analysis();

    private final AnalysisRunner runner = new AnalysisRunner();

    private AnalysisResult run(Questionnaires questionnaire) {
        return runner.run(ANALYSIS, new User(Questionnaires.USER_ID), questionnaire.answers());
    }

    @Test
    void moderateAcneResultHasRoutineSortedBySteps() {
        AnalysisResult result = run(TherapyRulesTest.moderateAcne());

        assertThat(result.getFototip()).isEqualTo(2);
        assertThat(result.getMed()).isEqualTo(30);
        assertThat(result.getDiagnoses()).extracting(d -> d.getCondition()).containsExactly(Condition.ACNE);
        assertThat(result.getMorningRoutine()).extracting(Recommendation::getStep, Recommendation::getSastojak)
                .containsExactly(
                        tuple(RoutineStep.CLEANSING, "blago sredstvo za čišćenje"),
                        tuple(RoutineStep.TREATMENT, "benzoil peroksid"),
                        tuple(RoutineStep.HYDRATION, "nekomedogena krema za hidrataciju"),
                        tuple(RoutineStep.SPF, "SPF 50"));
        assertThat(result.getEveningRoutine()).extracting(Recommendation::getSastojak)
                .containsExactly("blago sredstvo za čišćenje", "adapalen", "nekomedogena krema za hidrataciju");
        assertThat(result.getExcludedDiagnoses()).extracting(e -> e.getCondition())
                .contains(Condition.ATOPIC_DERMATITIS);
    }

    @Test
    void pregnantResultListsWithdrawnSeparately() {
        Questionnaires questionnaire = TherapyRulesTest.moderateAcne();
        questionnaire.lifestyle().setPregnant(true);

        AnalysisResult result = run(questionnaire);

        assertThat(result.getWithdrawn()).extracting(Recommendation::getSastojak).containsExactly("adapalen");
        assertThat(result.getEveningRoutine()).extracting(Recommendation::getSastojak)
                .contains("azelainska kiselina").doesNotContain("adapalen");
        assertThat(result.getEveningRoutine()).filteredOn(r -> r.getSastojak().equals("azelainska kiselina"))
                .extracting(Recommendation::getStatus).containsExactly(RecommendationStatus.PENDING_REVIEW);
        assertThat(result.getSafetyNotes()).isNotEmpty();
    }

    @Test
    void everyRecommendationNamesItsRuleAndSource() {
        AnalysisResult result = run(RosaceaRulesTest.pprModerate());

        assertThat(result.getMorningRoutine()).allSatisfy(r -> {
            assertThat(r.getRuleName()).isNotBlank();
            assertThat(r.getSource()).isNotBlank();
        });
        assertThat(result.getEveningRoutine()).allSatisfy(r -> assertThat(r.getRuleName()).isNotBlank());
    }

    @Test
    void explanationFollowsThePhases() {
        AnalysisResult result = run(TherapyRulesTest.moderateAcne());

        assertThat(result.getExplanation()).isNotEmpty();
        assertThat(result.getExplanation()).extracting(s -> s.getAgendaGroup())
                .containsSubsequence("phototype", "diagnosis", "therapy", "safety");
    }

    @Test
    void contactDermatitisCausesAreListed() {
        AnalysisResult result = run(ContactDermatitisTest.kathonReaction());

        assertThat(result.getContactDermatitisCauses())
                .containsExactly("kathon cg", "methylchloroisothiazolinone", "methylisothiazolinone");
        assertThat(result.getReferrals()).isEmpty();
    }

    @Test
    void severeCaseResultHasReferral() {
        Questionnaires questionnaire = RosaceaRulesTest.etr();
        questionnaire.rosacea().setOcularSymptoms(true);

        AnalysisResult result = run(questionnaire);

        assertThat(result.getReferrals()).extracting(r -> r.getSpecialist())
                .containsExactlyInAnyOrder("dermatolog", "oftalmolog");
    }
}
