package com.ftn.sbnz.service.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public record SunEntryRequest(
        Instant occurredAt,
        @NotNull @DecimalMin("0") @DecimalMax("20") Double uvIndex,
        @NotNull @Min(1) @Max(1440) Integer minutesInSun,
        @NotNull @Min(0) @Max(100) Integer sunscreenSpf) {
}
