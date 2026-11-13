package com.ftn.sbnz.kjar;

import com.ftn.sbnz.model.cep.CepAlert;
import com.ftn.sbnz.model.diary.UVDanEvent;
import com.ftn.sbnz.model.enums.CepAlertType;
import com.ftn.sbnz.model.facts.User;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CepUvTest {

    static User typeTwo() {
        User user = new User("u1");
        user.setFototip(2);
        user.setMed(30);
        return user;
    }

    static List<CepAlert> sun(CepSession session, Duration offset, double uv, int minutes, int spf) {
        Date time = CepFlushingTest.at(offset);
        return session.insert(new UVDanEvent("u1", time, uv, minutes, spf), time).alerts();
    }

    private static List<CepAlert> of(List<CepAlert> alerts, CepAlertType type) {
        return alerts.stream().filter(a -> a.getType() == type).toList();
    }

    @Test
    void doseAboveHalfMedGivesWarning() {
        try (CepSession session = new CepSession(CepSessionTest.CEP, typeTwo())) {
            List<CepAlert> alerts = sun(session, Duration.ofHours(5), 5, 30, 0);

            assertThat(of(alerts, CepAlertType.HIGH_UV_EXPOSURE)).singleElement()
                    .extracting(CepAlert::getMessage).asString()
                    .contains("22.5 od 30 mJ/cm²");
        }
    }

    @Test
    void sunscreenKeepsDoseBelowLimit() {
        try (CepSession session = new CepSession(CepSessionTest.CEP, typeTwo())) {
            assertThat(of(sun(session, Duration.ofHours(5), 8, 60, 30), CepAlertType.HIGH_UV_EXPOSURE)).isEmpty();
        }
    }

    @Test
    void smallDosesAddUpWithinTheDay() {
        try (CepSession session = new CepSession(CepSessionTest.CEP, typeTwo())) {
            List<CepAlert> alerts = new ArrayList<>();
            alerts.addAll(sun(session, Duration.ofHours(3), 3, 20, 0));
            alerts.addAll(sun(session, Duration.ofHours(6), 4, 10, 0));
            assertThat(of(alerts, CepAlertType.HIGH_UV_EXPOSURE)).isEmpty();

            alerts.addAll(sun(session, Duration.ofHours(8), 3, 10, 0));
            assertThat(of(alerts, CepAlertType.HIGH_UV_EXPOSURE)).hasSize(1);
        }
    }

    @Test
    void atMostOneWarningPerDay() {
        try (CepSession session = new CepSession(CepSessionTest.CEP, typeTwo())) {
            List<CepAlert> alerts = new ArrayList<>();
            alerts.addAll(sun(session, Duration.ofHours(3), 6, 40, 0));
            alerts.addAll(sun(session, Duration.ofHours(6), 7, 40, 0));
            alerts.addAll(sun(session, Duration.ofHours(9), 5, 40, 0));
            assertThat(of(alerts, CepAlertType.HIGH_UV_EXPOSURE)).hasSize(1);

            alerts.addAll(sun(session, Duration.ofDays(1).plusHours(4), 6, 40, 0));
            assertThat(of(alerts, CepAlertType.HIGH_UV_EXPOSURE)).hasSize(2);
        }
    }

    @Test
    void higherPhototypeToleratesMore() {
        User typeSix = new User("u1");
        typeSix.setFototip(6);
        typeSix.setMed(120);
        try (CepSession session = new CepSession(CepSessionTest.CEP, typeSix)) {
            assertThat(of(sun(session, Duration.ofHours(5), 5, 30, 0), CepAlertType.HIGH_UV_EXPOSURE)).isEmpty();
        }
    }

    @Test
    void veryHighUvWithoutSunscreenGivesImmediateAlert() {
        try (CepSession session = new CepSession(CepSessionTest.CEP, typeTwo())) {
            assertThat(of(sun(session, Duration.ofHours(5), 9, 45, 0), CepAlertType.VERY_HIGH_UV)).hasSize(1);
        }
        try (CepSession session = new CepSession(CepSessionTest.CEP, typeTwo())) {
            assertThat(of(sun(session, Duration.ofHours(5), 9, 45, 50), CepAlertType.VERY_HIGH_UV)).isEmpty();
        }
    }
}
