package com.ftn.sbnz.service.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TemplateRuleRequest(
        @NotBlank @Size(max = 60) String sastojak,
        @Min(1) @Max(6) int minFototip,
        @Min(1) @Max(6) int maxFototip,
        @DecimalMin(value = "0", inclusive = false) @DecimalMax("100") double maxKoncentracija,
        @NotBlank @Size(max = 300) String napomena) {
}
