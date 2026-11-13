package com.ftn.sbnz.service.dto;

import com.ftn.sbnz.model.enums.Condition;

import java.time.Instant;
import java.util.List;

public record AnalysisSummary(Long id, Instant createdAt, int fototip, List<Condition> conditions) {
}
