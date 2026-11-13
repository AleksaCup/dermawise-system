package com.ftn.sbnz.service.dto;

import com.ftn.sbnz.model.enums.Condition;
import com.ftn.sbnz.model.enums.RecommendationStatus;
import com.ftn.sbnz.model.enums.RoutineSlot;
import com.ftn.sbnz.model.enums.RoutineStep;
import com.ftn.sbnz.service.entity.RecommendationEntity;
import com.ftn.sbnz.service.entity.RecommendationOrigin;

import java.time.Instant;
import java.util.List;

public record RecommendationDto(
        Long id,
        RecommendationOrigin origin,
        Condition forCondition,
        String sastojak,
        Double koncentracija,
        String form,
        RoutineSlot slot,
        RoutineStep step,
        String frequency,
        boolean requiresPrescription,
        RecommendationStatus status,
        String reason,
        String source,
        String ruleName,
        List<String> warnings,
        String reviewComment,
        Instant createdAt) {

    public static RecommendationDto from(RecommendationEntity e) {
        return new RecommendationDto(e.getId(), e.getOrigin(), e.getForCondition(), e.getSastojak(),
                e.getKoncentracija(), e.getForm(), e.getSlot(), e.getStep(), e.getFrequency(),
                e.isRequiresPrescription(), e.getStatus(), e.getReason(), e.getSource(), e.getRuleName(),
                List.copyOf(e.getWarnings()), e.getReviewComment(), e.getCreatedAt());
    }
}
