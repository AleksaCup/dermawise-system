package com.ftn.sbnz.service.dto;

import java.time.Instant;

public record ProfileResponse(
        Long userId,
        String fullName,
        QuestionnaireRequest questionnaire,
        int fototip,
        double med,
        boolean pregnant,
        boolean breastfeeding,
        Instant updatedAt) {
}
