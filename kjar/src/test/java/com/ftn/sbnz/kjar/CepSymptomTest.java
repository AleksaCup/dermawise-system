package com.ftn.sbnz.kjar;

import com.ftn.sbnz.model.cep.CepAlert;
import com.ftn.sbnz.model.diary.NoviProizvod;
import com.ftn.sbnz.model.diary.SkinSymptom;
import com.ftn.sbnz.model.enums.BodyArea;
import com.ftn.sbnz.model.enums.CepAlertType;
import com.ftn.sbnz.model.enums.SymptomType;
import com.ftn.sbnz.model.facts.User;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CepSymptomTest {

    private static List<CepAlert> symptom(CepSession session, int day, SymptomType type, BodyArea area) {
        Date time = CepFlushingTest.at(Duration.ofDays(day).plusHours(10));
        return session.insert(new SkinSymptom("u1", time, type, area), time).alerts();
    }

    private static List<CepAlert> newProduct(CepSession session, int day) {
        Date time = CepFlushingTest.at(Duration.ofDays(day));
        return session.insert(new NoviProizvod("u1", time, ContactDermatitisTest.product("losion za lice", "kathon cg"), 0),
                time).alerts();
    }

    private static List<CepAlert> of(List<CepAlert> alerts, CepAlertType type) {
        return alerts.stream().filter(a -> a.getType() == type).toList();
    }

    @Test
    void repeatedScalingInSeborrheicAreasGivesOneAlert() {
        try (CepSession session = new CepSession(CepSessionTest.CEP, new User("u1"))) {
            List<CepAlert> alerts = new ArrayList<>();
            alerts.addAll(symptom(session, 0, SymptomType.SCALING, BodyArea.SCALP));
            alerts.addAll(symptom(session, 4, SymptomType.SCALING, BodyArea.EYEBROWS));
            alerts.addAll(symptom(session, 8, SymptomType.SCALING, BodyArea.NASOLABIAL_FOLDS));
            alerts.addAll(symptom(session, 9, SymptomType.SCALING, BodyArea.SCALP));

            assertThat(of(alerts, CepAlertType.SEBORRHEIC_SYMPTOMS)).hasSize(1);
        }
    }

    @Test
    void scalingOnHandsIsIgnored() {
        try (CepSession session = new CepSession(CepSessionTest.CEP, new User("u1"))) {
            List<CepAlert> alerts = new ArrayList<>();
            for (int day = 0; day < 6; day += 2) {
                alerts.addAll(symptom(session, day, SymptomType.SCALING, BodyArea.HANDS));
            }
            assertThat(of(alerts, CepAlertType.SEBORRHEIC_SYMPTOMS)).isEmpty();
        }
    }

    @Test
    void rednessWhereNewProductIsUsedGivesContactAlert() {
        try (CepSession session = new CepSession(CepSessionTest.CEP, new User("u1"))) {
            List<CepAlert> alerts = new ArrayList<>(newProduct(session, 0));
            alerts.addAll(symptom(session, 3, SymptomType.ERYTHEMA, BodyArea.CHEEKS));
            alerts.addAll(symptom(session, 4, SymptomType.STINGING, BodyArea.CHEEKS));

            assertThat(of(alerts, CepAlertType.CONTACT_REACTION)).singleElement()
                    .extracting(CepAlert::getMessage).asString().contains("losion za lice");
        }
    }

    @Test
    void rednessAfterFourteenDaysOrElsewhereIsNotContactReaction() {
        try (CepSession session = new CepSession(CepSessionTest.CEP, new User("u1"))) {
            List<CepAlert> alerts = new ArrayList<>(newProduct(session, 0));
            alerts.addAll(symptom(session, 2, SymptomType.ERYTHEMA, BodyArea.HANDS));
            alerts.addAll(symptom(session, 16, SymptomType.ERYTHEMA, BodyArea.CHEEKS));

            assertThat(of(alerts, CepAlertType.CONTACT_REACTION)).isEmpty();
        }
    }
}
