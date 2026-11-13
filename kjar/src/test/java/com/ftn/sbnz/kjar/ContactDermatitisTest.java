package com.ftn.sbnz.kjar;

import com.ftn.sbnz.model.diary.NoviProizvod;
import com.ftn.sbnz.model.enums.BodyArea;
import com.ftn.sbnz.model.enums.Condition;
import com.ftn.sbnz.model.enums.SymptomType;
import com.ftn.sbnz.model.facts.Diagnosis;
import com.ftn.sbnz.model.facts.Recommendation;
import com.ftn.sbnz.model.facts.SafetyNote;
import com.ftn.sbnz.model.questionnaire.Product;
import com.ftn.sbnz.model.questionnaire.ProductIngredient;
import org.junit.jupiter.api.Test;
import org.kie.api.runtime.rule.Variable;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class ContactDermatitisTest {

    private static final String U = Questionnaires.USER_ID;

    static Product product(String name, String... ingredients) {
        return new Product(name,
                Arrays.stream(ingredients).map(i -> new ProductIngredient(i, null)).toList(),
                Set.of(BodyArea.CHEEKS, BodyArea.FOREHEAD));
    }

    static Questionnaires reaction(Product product, int daysAgo, BodyArea symptomArea, String... allergies) {
        Questionnaires questionnaire = Questionnaires.standard()
                .fact(new NoviProizvod(U, new Date(), product, daysAgo))
                .symptom(SymptomType.ERYTHEMA, symptomArea)
                .symptom(SymptomType.STINGING, symptomArea);
        questionnaire.lifestyle().setAllergies(List.of(allergies));
        return questionnaire;
    }

    static Questionnaires kathonReaction() {
        return reaction(product("losion za lice", "aqua", "glycerin", "kathon cg"), 5, BodyArea.CHEEKS,
                "methylisothiazolinone");
    }

    @Test
    void kathonCgIsFoundThroughMethylisothiazolinone() {
        assertThat(culprits(kathonReaction()))
                .contains("kathon cg", "methylisothiazolinone")
                .doesNotContain("aqua", "glycerin");
    }

    @Test
    void crossReactiveGroupAddsTheOtherIsothiazolinone() {
        assertThat(culprits(kathonReaction())).containsExactlyInAnyOrder(
                "kathon cg", "methylisothiazolinone", "methylchloroisothiazolinone");
    }

    @Test
    void formaldehydeAllergyCatchesFormaldehydeReleaser() {
        Questionnaires questionnaire = reaction(product("šampon", "aqua", "dmdm hydantoin"), 3, BodyArea.FOREHEAD,
                "formaldehyde");

        assertThat(culprits(questionnaire)).contains("dmdm hydantoin", "formaldehyde");
    }

    @Test
    void paraGroupCrossReactivity() {
        Questionnaires questionnaire = reaction(product("krema za usne", "benzocaine"), 2, BodyArea.CHEEKS,
                "p-phenylenediamine");

        assertThat(culprits(questionnaire)).containsExactly("benzocaine");
    }

    @Test
    void productOlderThanFourteenDaysIsNotTheCause() {
        Questionnaires questionnaire = reaction(product("losion", "kathon cg"), 20, BodyArea.CHEEKS,
                "methylisothiazolinone");

        assertThat(culprits(questionnaire)).isEmpty();
    }

    @Test
    void fourteenDaysIsStillRecent() {
        Questionnaires questionnaire = reaction(product("losion", "kathon cg"), 14, BodyArea.CHEEKS,
                "methylisothiazolinone");

        assertThat(culprits(questionnaire)).isNotEmpty();
    }

    @Test
    void reactionOutsideApplicationAreaIsNotTheCause() {
        Questionnaires questionnaire = reaction(product("losion", "kathon cg"), 5, BodyArea.HANDS,
                "methylisothiazolinone");

        assertThat(culprits(questionnaire)).isEmpty();
    }

    @Test
    void noAllergyNoCause() {
        Questionnaires questionnaire = reaction(product("losion", "kathon cg"), 5, BodyArea.CHEEKS);

        assertThat(culprits(questionnaire)).isEmpty();
    }

    @Test
    void analysisDiagnosesContactDermatitisAndNamesCulprits() {
        try (AnalysisSession session = new AnalysisSession()) {
            session.insertAll(kathonReaction().facts()).fire();

            assertThat(session.all(Diagnosis.class)).extracting(Diagnosis::getCondition)
                    .containsExactly(Condition.CONTACT_DERMATITIS);
            assertThat(session.all(SafetyNote.class)).extracting(SafetyNote::getText).contains(
                    "Verovatan uzročnik reakcije: kathon cg",
                    "Verovatan uzročnik reakcije: methylisothiazolinone",
                    "Verovatan uzročnik reakcije: methylchloroisothiazolinone",
                    "Prekinuti proizvod koji je izazvao reakciju.");
            assertThat(session.all(Recommendation.class)).extracting(Recommendation::getSastojak)
                    .contains("hidrokortizon", "emolijens bez mirisa");
            assertThat(session.timesFired("Kontaktni dermatitis")).isEqualTo(1);
            assertThat(session.timesFired("Uzročnik kontaktnog dermatitisa")).isEqualTo(3);
        }
    }

    static List<String> culprits(Questionnaires questionnaire) {
        try (BackwardSession session = new BackwardSession()) {
            session.insertAll(questionnaire.facts()).prepare();
            return session.column("uzrocnikKontaktnogDermatitisa", "sastojak", U, Variable.v).stream()
                    .distinct().toList();
        }
    }
}
