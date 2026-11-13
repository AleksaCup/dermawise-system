package com.ftn.sbnz.service.dto;

import com.ftn.sbnz.model.enums.FlushTrigger;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public record FlushingEntryRequest(
        Instant occurredAt,
        @NotNull @Min(1) @Max(600) Integer durationMinutes,
        FlushTrigger trigger) {
}
