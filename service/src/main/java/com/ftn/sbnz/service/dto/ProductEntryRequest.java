package com.ftn.sbnz.service.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public record ProductEntryRequest(Instant occurredAt, @NotNull @Valid ProductDto product) {
}
