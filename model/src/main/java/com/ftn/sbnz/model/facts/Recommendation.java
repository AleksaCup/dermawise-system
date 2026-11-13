package com.ftn.sbnz.model.facts;

import com.ftn.sbnz.model.enums.Condition;
import com.ftn.sbnz.model.enums.RecommendationStatus;
import com.ftn.sbnz.model.enums.RoutineSlot;
import com.ftn.sbnz.model.enums.RoutineStep;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Recommendation {

    @Builder.Default
    private String id = UUID.randomUUID().toString();
    private String userId;
    private Condition forCondition;

    private String sastojak;
    private Double koncentracija;
    private String form;
    private RoutineSlot slot;
    private RoutineStep step;
    private String frequency;

    private boolean requiresPrescription;
    @Builder.Default
    private RecommendationStatus status = RecommendationStatus.ACTIVE;

    private String reason;
    private String source;
    private String ruleName;

    @Builder.Default
    private List<String> warnings = new ArrayList<>();

    public void addWarning(String warning) {
        warnings.add(warning);
    }

    public boolean isActive() {
        return status != RecommendationStatus.WITHDRAWN && status != RecommendationStatus.REJECTED;
    }
}
