package com.ftn.sbnz.kjar;

import com.ftn.sbnz.model.facts.SafetyNote;
import com.ftn.sbnz.model.facts.SeverityScore;
import org.junit.jupiter.api.Test;
import org.kie.api.KieBase;
import org.kie.api.runtime.KieSession;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FirstSessionTest {

    private static final String TEMPLATE = "templates/test-threshold.drt";

    private final TemplateCompiler templateCompiler = new TemplateCompiler();
    private final KnowledgeBaseFactory factory = new KnowledgeBaseFactory();

    @Test
    void templateCreatesOneRulePerRow() {
        String drl = templateCompiler.compile(TEMPLATE, List.of(
                new ThresholdRow("GAGS", 19),
                new ThresholdRow("SCORAD", 25)));

        assertThat(drl).contains("rule \"Test prag 0\"", "rule \"Test prag 1\"");
        assertThat(drl).contains("scale == \"GAGS\"", "value >= 19.0");
    }

    @Test
    void generatedRuleFiresInSession() {
        String drl = templateCompiler.compile(TEMPLATE, List.of(
                new ThresholdRow("GAGS", 19),
                new ThresholdRow("SCORAD", 25)));
        KieBase kieBase = factory.buildAll(List.of(drl)).analysis();

        KieSession session = kieBase.newKieSession();
        try {
            session.insert(new SeverityScore("u1", "GAGS", 23));
            int fired = session.fireAllRules();

            List<SafetyNote> notes = session.getObjects(o -> o instanceof SafetyNote).stream()
                    .map(SafetyNote.class::cast)
                    .toList();
            assertThat(fired).isEqualTo(1);
            assertThat(notes).extracting(SafetyNote::getText).containsExactly("Prag 19.0 za GAGS");
        } finally {
            session.dispose();
        }
    }

    @Test
    void missingTemplateIsReported() {
        assertThatThrownBy(() -> templateCompiler.compile("templates/nema.drt", List.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("templates/nema.drt");
    }
}
