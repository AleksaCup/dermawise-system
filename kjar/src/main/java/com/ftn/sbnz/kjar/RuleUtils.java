package com.ftn.sbnz.kjar;

import com.ftn.sbnz.model.enums.Condition;
import com.ftn.sbnz.model.enums.RecommendationStatus;
import com.ftn.sbnz.model.enums.RoutineSlot;
import com.ftn.sbnz.model.enums.RoutineStep;
import com.ftn.sbnz.model.facts.Recommendation;
import com.ftn.sbnz.model.facts.Referral;
import org.kie.api.definition.rule.Rule;

public final class RuleUtils {

    private RuleUtils() {
    }

    public static Recommendation recommendation(Rule rule, String userId, Condition condition,
                                                String sastojak, Double koncentracija, String form,
                                                RoutineSlot slot, RoutineStep step, String frequency,
                                                boolean prescription, String reason) {
        return Recommendation.builder()
                .userId(userId)
                .forCondition(condition)
                .sastojak(sastojak)
                .koncentracija(koncentracija)
                .form(form)
                .slot(slot)
                .step(step)
                .frequency(frequency)
                .requiresPrescription(prescription)
                .status(prescription ? RecommendationStatus.PENDING_REVIEW : RecommendationStatus.ACTIVE)
                .reason(reason)
                .source(source(rule))
                .ruleName(rule.getName())
                .build();
    }

    public static Referral referral(Rule rule, String userId, Condition condition, String specialist, String reason) {
        return new Referral(userId, condition, specialist, reason, source(rule));
    }

    public static String source(Rule rule) {
        Object source = rule.getMetaData().get("source");
        return source == null ? null : source.toString();
    }
}
