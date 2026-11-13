package com.ftn.sbnz.service.dto;

import java.time.Instant;

public record PatientSummary(
        Long id,
        String fullName,
        String email,
        boolean questionnaireFilled,
        int fototip,
        double med,
        boolean pregnant,
        Instant lastAnalysisAt,
        long pendingReviews) {
}
