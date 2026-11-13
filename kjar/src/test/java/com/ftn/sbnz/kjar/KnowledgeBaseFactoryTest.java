package com.ftn.sbnz.kjar;

import org.drools.kiesession.rulebase.InternalKnowledgeBase;
import org.junit.jupiter.api.Test;
import org.kie.api.KieBase;
import org.kie.api.conf.EventProcessingOption;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class KnowledgeBaseFactoryTest {

    private final KnowledgeBaseFactory factory = new KnowledgeBaseFactory();

    @Test
    void buildsAllThreeKnowledgeBases() {
        KnowledgeBases bases = factory.buildAll(List.of());

        assertThat(bases.analysis()).isNotNull();
        assertThat(bases.backward()).isNotNull();
        assertThat(bases.cep()).isNotNull();
    }

    @Test
    void onlyCepKnowledgeBaseIsInStreamMode() {
        KnowledgeBases bases = factory.buildAll(List.of());

        assertThat(isStream(bases.cep())).isTrue();
        assertThat(isStream(bases.analysis())).isFalse();
        assertThat(isStream(bases.backward())).isFalse();
    }

    @Test
    void invalidRuleStopsTheBuild() {
        String broken = "package rules;\nrule \"Neispravno\" when NepostojecaKlasa() then end";

        assertThatThrownBy(() -> factory.buildAll(List.of(broken)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("NepostojecaKlasa");
    }

    private boolean isStream(KieBase kieBase) {
        return ((InternalKnowledgeBase) kieBase).getRuleBaseConfiguration().getEventProcessingMode()
                == EventProcessingOption.STREAM;
    }
}
