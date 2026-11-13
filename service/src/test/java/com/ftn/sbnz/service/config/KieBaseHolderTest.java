package com.ftn.sbnz.service.config;

import org.junit.jupiter.api.Test;
import org.kie.api.KieBase;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class KieBaseHolderTest {

    private static final String VALID_RULE = """
            package rules;
            rule "Dodato pravilo"
            when
            then
            end
            """;

    private static final String BROKEN_RULE = """
            package rules;
            rule "Neispravno" when NepostojecaKlasa() then end
            """;

    @Test
    void startsWithAllKnowledgeBases() {
        KieBaseHolder holder = new KieBaseHolder();

        assertThat(holder.getAnalysisKieBase()).isNotNull();
        assertThat(holder.getBackwardKieBase()).isNotNull();
        assertThat(holder.getCepKieBase()).isNotNull();
    }

    @Test
    void rebuildSwapsInNewRules() {
        KieBaseHolder holder = new KieBaseHolder();
        KieBase before = holder.getAnalysisKieBase();

        holder.rebuild(List.of(VALID_RULE));

        assertThat(holder.getAnalysisKieBase()).isNotSameAs(before);
        assertThat(holder.getAnalysisKieBase().getRule("rules", "Dodato pravilo")).isNotNull();
        assertThat(holder.getCepKieBase().getRule("rules", "Dodato pravilo")).isNotNull();
    }

    @Test
    void failedRebuildKeepsOldKnowledgeBases() {
        KieBaseHolder holder = new KieBaseHolder();
        KieBase analysis = holder.getAnalysisKieBase();
        KieBase cep = holder.getCepKieBase();

        assertThatThrownBy(() -> holder.rebuild(List.of(BROKEN_RULE)))
                .isInstanceOf(IllegalStateException.class);

        assertThat(holder.getAnalysisKieBase()).isSameAs(analysis);
        assertThat(holder.getCepKieBase()).isSameAs(cep);
    }
}
