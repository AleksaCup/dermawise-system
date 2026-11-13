package com.ftn.sbnz.service.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import java.util.List;

public record LifestyleSection(
        boolean pregnant,
        boolean breastfeeding,
        boolean hormonalTherapy,
        List<String> allergies,
        List<@Valid ProductDto> currentProducts,
        @Min(0) @Max(1440) int dailySunMinutes) {
}
