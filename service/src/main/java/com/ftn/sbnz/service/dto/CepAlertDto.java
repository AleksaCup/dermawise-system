package com.ftn.sbnz.service.dto;

import com.ftn.sbnz.model.enums.CepAlertType;
import com.ftn.sbnz.service.entity.CepAlertEntity;

import java.time.Instant;

public record CepAlertDto(Long id, CepAlertType type, String message, Instant occurredAt, boolean seen) {

    public static CepAlertDto from(CepAlertEntity e) {
        return new CepAlertDto(e.getId(), e.getType(), e.getMessage(), e.getOccurredAt(), e.isSeen());
    }
}
