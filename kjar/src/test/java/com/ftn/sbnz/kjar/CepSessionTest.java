package com.ftn.sbnz.kjar;

import com.ftn.sbnz.kjar.template.MaxConcentrationRows;
import com.ftn.sbnz.model.facts.User;
import org.junit.jupiter.api.Test;
import org.kie.api.KieBase;

import java.time.Instant;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

class CepSessionTest {

    static final KieBase CEP =
            new KnowledgeBaseFactory().buildWithMaxConcentration(MaxConcentrationRows.load()).cep();

    @Test
    void clockOnlyMovesForward() {
        try (CepSession session = new CepSession(CEP, new User("u1"))) {
            Date later = Date.from(Instant.parse("2026-11-10T12:00:00Z"));
            Date earlier = Date.from(Instant.parse("2026-11-01T12:00:00Z"));

            session.advanceTo(later);
            session.advanceTo(earlier);

            assertThat(session.now()).isEqualTo(later);
        }
    }

    @Test
    void emptySessionGivesEmptyResult() {
        try (CepSession session = new CepSession(CEP, new User("u1"))) {
            assertThat(session.advanceTo(new Date(1000)).isEmpty()).isTrue();
        }
    }
}
