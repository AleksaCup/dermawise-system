package com.ftn.sbnz.service.config;

import com.ftn.sbnz.kjar.template.MaxConcentrationRows;
import com.ftn.sbnz.model.template.MaxConcentrationByPhototype;
import org.junit.jupiter.api.Test;
import org.kie.api.KieBase;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class KieBaseHolderTest {

    @Test
    void startsWithAllKnowledgeBases() {
        KieBaseHolder holder = new KieBaseHolder();

        assertThat(holder.getAnalysisKieBase()).isNotNull();
        assertThat(holder.getBackwardKieBase()).isNotNull();
        assertThat(holder.getCepKieBase()).isNotNull();
        assertThat(holder.getVersion()).isEqualTo(1);
    }

    @Test
    void rebuildSwapsInTemplateRules() {
        KieBaseHolder holder = new KieBaseHolder();
        KieBase before = holder.getAnalysisKieBase();

        holder.rebuild(MaxConcentrationRows.load());

        assertThat(holder.getAnalysisKieBase()).isNotSameAs(before);
        assertThat(holder.getAnalysisKieBase().getRule("rules", "Provera bezbedne koncentracije 4")).isNotNull();
        assertThat(holder.getCepKieBase().getRule("rules", "Provera bezbedne koncentracije 4")).isNotNull();
        assertThat(holder.getVersion()).isEqualTo(2);
        assertThat(holder.getTemplateRows()).isEqualTo(5);
    }

    @Test
    void failedRebuildKeepsOldKnowledgeBases() {
        KieBaseHolder holder = new KieBaseHolder();
        KieBase analysis = holder.getAnalysisKieBase();
        List<MaxConcentrationByPhototype> rows = new ArrayList<>(MaxConcentrationRows.load());
        rows.add(new MaxConcentrationByPhototype("los\" red", 1, 6, 1.0, "napomena"));

        assertThatThrownBy(() -> holder.rebuild(rows)).isInstanceOf(IllegalStateException.class);

        assertThat(holder.getAnalysisKieBase()).isSameAs(analysis);
        assertThat(holder.getVersion()).isEqualTo(1);
    }
}
