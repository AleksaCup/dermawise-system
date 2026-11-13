package com.ftn.sbnz.service.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record ScoradSection(
        @DecimalMin("0") @DecimalMax("100") double affectedAreaPercent,
        @Min(0) @Max(3) int erythema,
        @Min(0) @Max(3) int edemaPapulation,
        @Min(0) @Max(3) int excoriation,
        @Min(0) @Max(3) int lichenification,
        @Min(0) @Max(3) int oozingCrusting,
        @Min(0) @Max(3) int dryness,
        @Min(0) @Max(10) int pruritus,
        @Min(0) @Max(10) int sleeplessness) {
}
