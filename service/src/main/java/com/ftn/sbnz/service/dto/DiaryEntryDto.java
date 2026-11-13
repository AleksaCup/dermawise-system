package com.ftn.sbnz.service.dto;

import com.ftn.sbnz.model.enums.BodyArea;
import com.ftn.sbnz.model.enums.FlushTrigger;
import com.ftn.sbnz.model.enums.SymptomType;
import com.ftn.sbnz.service.entity.DiaryEntry;
import com.ftn.sbnz.service.entity.DiaryEntryType;

import java.time.Instant;

public record DiaryEntryDto(
        Long id,
        DiaryEntryType type,
        Instant occurredAt,
        Integer durationMinutes,
        FlushTrigger trigger,
        Double uvIndex,
        Integer minutesInSun,
        Integer sunscreenSpf,
        String productName,
        SymptomType symptomType,
        BodyArea symptomLocation) {

    public static DiaryEntryDto from(DiaryEntry e) {
        return new DiaryEntryDto(e.getId(), e.getType(), e.getOccurredAt(), e.getDurationMinutes(),
                e.getFlushTrigger(), e.getUvIndex(), e.getMinutesInSun(), e.getSunscreenSpf(), e.getProductName(),
                e.getSymptomType(), e.getSymptomLocation());
    }
}
