package com.ftn.sbnz.kjar;

import com.ftn.sbnz.model.cep.CepAlert;
import com.ftn.sbnz.model.diary.FlushingEpizoda;
import com.ftn.sbnz.model.enums.CepAlertType;
import com.ftn.sbnz.model.enums.FlushTrigger;
import com.ftn.sbnz.model.facts.User;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CepFlushingTest {

    static final Instant START = Instant.parse("2026-11-02T07:00:00Z");

    static Date at(Duration offset) {
        return Date.from(START.plus(offset));
    }

    private static List<CepAlert> flush(CepSession session, Duration offset, FlushTrigger trigger) {
        Date time = at(offset);
        return session.insert(new FlushingEpizoda("u1", time, 15, trigger), time).alerts();
    }

    @Test
    void fourEpisodesInSevenDaysGiveOneSuspicion() {
        try (CepSession session = new CepSession(CepSessionTest.CEP, new User("u1"))) {
            List<CepAlert> alerts = new ArrayList<>();
            alerts.addAll(flush(session, Duration.ofDays(0), FlushTrigger.STRES));
            alerts.addAll(flush(session, Duration.ofDays(1), FlushTrigger.HRANA));
            alerts.addAll(flush(session, Duration.ofDays(3), FlushTrigger.TOPLOTA));
            assertThat(alerts).isEmpty();

            List<CepAlert> fourth = flush(session, Duration.ofDays(5), FlushTrigger.KOZMETIKA);
            assertThat(fourth).singleElement().satisfies(alert -> {
                assertThat(alert.getType()).isEqualTo(CepAlertType.ROSACEA_SUSPICION);
                assertThat(alert.getMessage()).startsWith("Detektovano 4 epizoda flushing-a u poslednjih 7 dana");
            });

            List<CepAlert> fifth = flush(session, Duration.ofDays(6), FlushTrigger.STRES);
            assertThat(fifth).isEmpty();
        }
    }

    @Test
    void episodesSpreadOverMoreThanSevenDaysGiveNoSuspicion() {
        try (CepSession session = new CepSession(CepSessionTest.CEP, new User("u1"))) {
            List<CepAlert> alerts = new ArrayList<>();
            for (int day = 0; day < 16; day += 4) {
                alerts.addAll(flush(session, Duration.ofDays(day), FlushTrigger.STRES));
            }
            assertThat(alerts).noneMatch(a -> a.getType() == CepAlertType.ROSACEA_SUSPICION);
        }
    }

    @Test
    void suspicionCanComeBackAfterAWeek() {
        try (CepSession session = new CepSession(CepSessionTest.CEP, new User("u1"))) {
            List<CepAlert> alerts = new ArrayList<>();
            for (int i = 0; i < 4; i++) {
                alerts.addAll(flush(session, Duration.ofHours(i * 12), null));
            }
            for (int i = 0; i < 4; i++) {
                alerts.addAll(flush(session, Duration.ofDays(8).plusHours(i * 12), null));
            }
            assertThat(alerts).filteredOn(a -> a.getType() == CepAlertType.ROSACEA_SUSPICION).hasSize(2);
        }
    }

    @Test
    void thirdEpisodeWithSameTriggerMakesItDominant() {
        try (CepSession session = new CepSession(CepSessionTest.CEP, new User("u1"))) {
            List<CepAlert> alerts = new ArrayList<>();
            alerts.addAll(flush(session, Duration.ofDays(0), FlushTrigger.ALKOHOL));
            alerts.addAll(flush(session, Duration.ofDays(9), FlushTrigger.ALKOHOL));
            alerts.addAll(flush(session, Duration.ofDays(15), FlushTrigger.HRANA));
            alerts.addAll(flush(session, Duration.ofDays(20), FlushTrigger.ALKOHOL));
            alerts.addAll(flush(session, Duration.ofDays(25), FlushTrigger.ALKOHOL));

            assertThat(alerts).filteredOn(a -> a.getType() == CepAlertType.DOMINANT_TRIGGER)
                    .singleElement().extracting(CepAlert::getMessage).asString()
                    .startsWith("Identifikovan okidač: alkohol (3 epizoda");
        }
    }

    @Test
    void threeSameTriggersOverMoreThanThirtyDaysAreNotDominant() {
        try (CepSession session = new CepSession(CepSessionTest.CEP, new User("u1"))) {
            List<CepAlert> alerts = new ArrayList<>();
            alerts.addAll(flush(session, Duration.ofDays(0), FlushTrigger.TOPLOTA));
            alerts.addAll(flush(session, Duration.ofDays(20), FlushTrigger.TOPLOTA));
            alerts.addAll(flush(session, Duration.ofDays(35), FlushTrigger.TOPLOTA));

            assertThat(alerts).noneMatch(a -> a.getType() == CepAlertType.DOMINANT_TRIGGER);
        }
    }
}
