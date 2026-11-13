package com.ftn.sbnz.kjar;

import com.ftn.sbnz.model.cep.CepAlert;
import com.ftn.sbnz.model.enums.CepAlertType;
import com.ftn.sbnz.model.enums.RecommendationStatus;
import com.ftn.sbnz.model.facts.Recommendation;
import com.ftn.sbnz.model.facts.SafetyNote;
import com.ftn.sbnz.model.facts.User;
import com.ftn.sbnz.model.diary.UVDanEvent;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

class CepPhotoagingTest {

    private record Collected(List<CepAlert> alerts, List<Recommendation> recommendations, List<SafetyNote> notes) {
    }

    private static Collected sunnyDays(User user, int... days) {
        List<CepAlert> alerts = new ArrayList<>();
        List<Recommendation> recommendations = new ArrayList<>();
        List<SafetyNote> notes = new ArrayList<>();
        try (CepSession session = new CepSession(CepSessionTest.CEP, user)) {
            for (int day : days) {
                Date time = CepFlushingTest.at(Duration.ofDays(day).plusHours(5));
                CepResult result = session.insert(new UVDanEvent("u1", time, 7, 60, 0), time);
                alerts.addAll(result.alerts());
                recommendations.addAll(result.recommendations());
                notes.addAll(result.notes());
            }
        }
        return new Collected(alerts, recommendations, notes);
    }

    @Test
    void fiveWarningsInThirtyDaysGiveGlogauRiskAndTreatment() {
        Collected collected = sunnyDays(CepUvTest.typeTwo(), 0, 3, 7, 12, 20);

        assertThat(collected.alerts()).filteredOn(a -> a.getType() == CepAlertType.HIGH_UV_EXPOSURE).hasSize(5);
        assertThat(collected.alerts()).filteredOn(a -> a.getType() == CepAlertType.PHOTOAGING_RISK).hasSize(1);
        assertThat(collected.recommendations())
                .extracting(Recommendation::getSastojak, Recommendation::getKoncentracija, Recommendation::getStatus)
                .containsExactlyInAnyOrder(
                        tuple("vitamin c", 15.0, RecommendationStatus.ACTIVE),
                        tuple("tretinoin", 0.025, RecommendationStatus.PENDING_REVIEW));
    }

    @Test
    void fourWarningsAreNotEnough() {
        Collected collected = sunnyDays(CepUvTest.typeTwo(), 0, 3, 7, 12);

        assertThat(collected.alerts()).noneMatch(a -> a.getType() == CepAlertType.PHOTOAGING_RISK);
    }

    @Test
    void warningsSpreadOverMoreThanThirtyDaysAreNotEnough() {
        Collected collected = sunnyDays(CepUvTest.typeTwo(), 0, 10, 20, 32, 40);

        assertThat(collected.alerts()).noneMatch(a -> a.getType() == CepAlertType.PHOTOAGING_RISK);
    }

    @Test
    void pregnantUserGetsNoTretinoin() {
        User user = CepUvTest.typeTwo();
        user.setPregnant(true);

        Collected collected = sunnyDays(user, 0, 3, 7, 12, 20);

        assertThat(collected.recommendations()).filteredOn(r -> r.getSastojak().equals("tretinoin"))
                .extracting(Recommendation::getStatus).containsOnly(RecommendationStatus.WITHDRAWN);
        assertThat(collected.recommendations()).extracting(Recommendation::getSastojak)
                .contains("vitamin c", "azelainska kiselina");
        assertThat(collected.notes()).extracting(SafetyNote::getText)
                .contains("Tretinoin povučen - prijavljena trudnoća, zamenjen azelainskom kiselinom 15%");
    }
}
