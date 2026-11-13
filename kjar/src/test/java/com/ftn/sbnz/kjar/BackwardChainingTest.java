package com.ftn.sbnz.kjar;

import com.ftn.sbnz.model.diary.FlushingSummary;
import com.ftn.sbnz.model.enums.Condition;
import com.ftn.sbnz.model.enums.FaceRegion;
import com.ftn.sbnz.model.enums.LesionType;
import com.ftn.sbnz.model.enums.RosaceaSubtype;
import com.ftn.sbnz.model.facts.Diagnosis;
import com.ftn.sbnz.model.enums.FlushTrigger;
import com.ftn.sbnz.model.facts.NalazCode;
import com.ftn.sbnz.model.questionnaire.Product;
import com.ftn.sbnz.model.questionnaire.ProductIngredient;
import org.junit.jupiter.api.Test;
import org.kie.api.runtime.rule.Variable;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

class BackwardChainingTest {

    private static final String U = Questionnaires.USER_ID;

    @Test
    void primarySignsAreProvenThroughTheirLeaves() {
        Questionnaires questionnaire = RosaceaRulesTest.etr();
        questionnaire.rosacea().setFlushingPerWeek(3);

        try (BackwardSession session = new BackwardSession()) {
            session.insertAll(questionnaire.facts()).prepare();

            assertThat(session.column("PrimarniZnakRosaceje", "znak", U, Variable.v))
                    .containsExactlyInAnyOrder(NalazCode.PERZISTENTNI_ERITEM, NalazCode.TELANGIEKTAZIJE,
                            NalazCode.FLUSHING_UPITNIK);
        }
    }

    @Test
    void transientErythemaFromDiaryNeedsTwoEpisodesAndTypicalTrigger() {
        try (BackwardSession session = new BackwardSession()) {
            session.insert(new FlushingSummary(U, 3, Set.of(FlushTrigger.ALKOHOL)));
            assertThat(session.size("TranzijentniEritem", U)).isEqualTo(1);
        }
        try (BackwardSession session = new BackwardSession()) {
            session.insert(new FlushingSummary(U, 3, Set.of(FlushTrigger.STRES)));
            assertThat(session.size("TranzijentniEritem", U)).isZero();
        }
        try (BackwardSession session = new BackwardSession()) {
            session.insert(new FlushingSummary(U, 1, Set.of(FlushTrigger.HRANA)));
            assertThat(session.size("TranzijentniEritem", U)).isZero();
        }
    }

    @Test
    void imaRosacejuReturnsLeavesOfTheProof() {
        Questionnaires questionnaire = RosaceaRulesTest.etr().flushingDiary(4, FlushTrigger.ALKOHOL, FlushTrigger.STRES);

        try (BackwardSession session = new BackwardSession()) {
            session.insertAll(questionnaire.facts()).prepare();

            assertThat(session.column("imaRosaceju", "$znak", U)).containsExactlyInAnyOrder(
                    NalazCode.PERZISTENTNI_ERITEM, NalazCode.TELANGIEKTAZIJE, NalazCode.FLUSHING_DNEVNIK);
        }
    }

    @Test
    void imaRosacejuIsFalseWithComedones() {
        Questionnaires questionnaire = RosaceaRulesTest.etr().lesion(FaceRegion.NOSE, LesionType.COMEDONES);

        try (BackwardSession session = new BackwardSession()) {
            session.insertAll(questionnaire.facts()).prepare();

            assertThat(session.size("imaRosaceju", U)).isZero();
        }
    }

    @Test
    void imaRosacejuIsFalseWithoutCentrofacialDistribution() {
        Questionnaires questionnaire = RosaceaRulesTest.etr();
        questionnaire.rosacea().setCentrofacialDistribution(false);

        try (BackwardSession session = new BackwardSession()) {
            session.insertAll(questionnaire.facts()).prepare();

            assertThat(session.size("imaRosaceju", U)).isZero();
        }
    }

    @Test
    void diaryFlushingAloneMeetsTheRosaceaCriterion() {
        Questionnaires questionnaire = Questionnaires.standard().flushingDiary(5, FlushTrigger.TOPLOTA);
        questionnaire.rosacea().setPersistentErythema(true);
        questionnaire.rosacea().setCentrofacialDistribution(true);

        try (AnalysisSession session = new AnalysisSession()) {
            session.insertAll(questionnaire.facts()).fire();

            assertThat(session.all(Diagnosis.class)).extracting(Diagnosis::getCondition, Diagnosis::getSubtype)
                    .containsExactly(tuple(Condition.ROSACEA, RosaceaSubtype.ETR));
            assertThat(session.timesFired("Nalaz flushing iz dnevnika")).isEqualTo(1);
            assertThat(session.timesFired("Primarni kriterijum rozaceje")).isEqualTo(1);
        }
    }

    @Test
    void imaAkneNeedsComedonesAndGags() {
        try (BackwardSession session = new BackwardSession()) {
            session.insertAll(SeverityScoreTest.proposalExample().facts()).prepare();
            assertThat(session.column("imaAkne", "$znak", U)).containsExactly(NalazCode.KOMEDONI);
        }
        try (BackwardSession session = new BackwardSession()) {
            session.insertAll(RosaceaRulesTest.pprModerate().facts()).prepare();
            assertThat(session.size("imaAkne", U)).isZero();
        }
    }

    @Test
    void imaPerioralniDermatitis() {
        try (BackwardSession session = new BackwardSession()) {
            session.insertAll(PerioralRulesTest.perioral().facts()).prepare();
            assertThat(session.column("imaPerioralniDermatitis", "$znak", U)).containsExactly(NalazCode.SKORASNJI_STEROID);
        }
    }

    @Test
    void ingredientBelongsToGroup() {
        try (BackwardSession session = new BackwardSession()) {
            assertThat(session.size("pripada", "adapalen", "retinoidi")).isEqualTo(1);
            assertThat(session.size("pripada", "benzoil peroksid", "retinoidi")).isZero();
            assertThat(session.column("pripada", "grupa", "doksiciklin", Variable.v)).containsExactly("tetraciklini");
        }
    }

    @Test
    void kathonContainsBothIsothiazolinones() {
        try (BackwardSession session = new BackwardSession()) {
            assertThat(session.column("sadrzi", "y", "kathon cg", Variable.v))
                    .containsExactlyInAnyOrder("methylisothiazolinone", "methylchloroisothiazolinone");
            assertThat(session.column("povezan", "y", "kathon cg", Variable.v))
                    .containsExactlyInAnyOrder("methylisothiazolinone", "methylchloroisothiazolinone",
                            "isothiazolinones", "isothiazolinones");
        }
    }

    @Test
    void steroidInCurrentProductCountsAsRecentSteroid() {
        Questionnaires questionnaire = PerioralRulesTest.perioral();
        questionnaire.rosacea().setRecentTopicalSteroid(false);
        questionnaire.lifestyle().setCurrentProducts(List.of(new Product("krema protiv svraba",
                List.of(new ProductIngredient("aqua", null), new ProductIngredient("hidrokortizon", 1.0)), Set.of())));

        assertThat(PerioralRulesTest.hasPerioral(questionnaire)).isTrue();
    }
}
