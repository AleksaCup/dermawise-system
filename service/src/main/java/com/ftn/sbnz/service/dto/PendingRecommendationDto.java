package com.ftn.sbnz.service.dto;

public record PendingRecommendationDto(Long patientId, String patientName, boolean pregnant,
                                       RecommendationDto recommendation) {
}
