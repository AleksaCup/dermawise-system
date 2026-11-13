package com.ftn.sbnz.service.dto;

import com.ftn.sbnz.model.enums.BodyArea;
import com.ftn.sbnz.model.enums.SymptomType;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public record SymptomEntryRequest(Instant occurredAt, @NotNull SymptomType type, @NotNull BodyArea location) {
}
