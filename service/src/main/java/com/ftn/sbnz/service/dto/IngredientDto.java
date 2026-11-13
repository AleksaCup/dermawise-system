package com.ftn.sbnz.service.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;

public record IngredientDto(@NotBlank String name, @DecimalMin("0") @DecimalMax("100") Double concentration) {
}
