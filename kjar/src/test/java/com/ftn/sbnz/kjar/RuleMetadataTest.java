package com.ftn.sbnz.kjar;

import com.ftn.sbnz.kjar.template.MaxConcentrationRows;
import org.drools.base.definitions.rule.impl.RuleImpl;
import org.junit.jupiter.api.Test;
import org.kie.api.KieBase;
import org.kie.api.definition.KiePackage;
import org.kie.api.definition.rule.Rule;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RuleMetadataTest {

    private static final KnowledgeBases BASES =
            new KnowledgeBaseFactory().buildWithMaxConcentration(MaxConcentrationRows.load());

    @Test
    void everyRuleHasSourceAndDescription() {
        for (KieBase kieBase : List.of(BASES.analysis(), BASES.backward(), BASES.cep())) {
            List<String> missing = new ArrayList<>();
            for (Rule rule : rules(kieBase)) {
                if (isBlank(rule.getMetaData().get("source")) || isBlank(rule.getMetaData().get("opis"))) {
                    missing.add(rule.getName());
                }
            }
            assertThat(missing).isEmpty();
        }
    }

    @Test
    void everyRuleBelongsToAPhase() {
        List<String> phases = List.of("phototype", "diagnosis", "therapy", "safety");
        for (Rule rule : rules(BASES.analysis())) {
            assertThat(((RuleImpl) rule).getAgendaGroup()).as(rule.getName()).isIn(phases);
        }
    }

    @Test
    void analysisBaseContainsTemplateRules() {
        List<String> names = rules(BASES.analysis()).stream().map(Rule::getName).toList();

        assertThat(names).contains("Kategorija GAGS MODERATE 1", "Provera bezbedne koncentracije 0",
                "Upozorenje za proizvod u upotrebi 4");
    }

    static List<Rule> rules(KieBase kieBase) {
        List<Rule> rules = new ArrayList<>();
        for (KiePackage kiePackage : kieBase.getKiePackages()) {
            for (Rule rule : kiePackage.getRules()) {
                if (!((RuleImpl) rule).isQuery()) {
                    rules.add(rule);
                }
            }
        }
        return rules;
    }

    private static boolean isBlank(Object value) {
        return value == null || value.toString().isBlank();
    }
}
