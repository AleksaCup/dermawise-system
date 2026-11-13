package com.ftn.sbnz.service.dto;

import com.ftn.sbnz.model.enums.BodyArea;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

import java.util.List;
import java.util.Set;

public record ProductDto(
        @NotBlank String name,
        List<@Valid IngredientDto> ingredients,
        Set<BodyArea> applicationAreas) {
}
