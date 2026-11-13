package com.ftn.sbnz.service.dto;

import com.ftn.sbnz.service.entity.MaxConcentrationRuleEntity;

import java.time.Instant;

public record TemplateRuleDto(
        Long id,
        String sastojak,
        int minFototip,
        int maxFototip,
        double maxKoncentracija,
        String napomena,
        String updatedBy,
        Instant updatedAt) {

    public static TemplateRuleDto from(MaxConcentrationRuleEntity e) {
        return new TemplateRuleDto(e.getId(), e.getSastojak(), e.getMinFototip(), e.getMaxFototip(),
                e.getMaxKoncentracija(), e.getNapomena(), e.getUpdatedBy(), e.getUpdatedAt());
    }
}
