package com.ftn.sbnz.service.dto;

import java.util.List;

public record DiaryEntryResponse(DiaryEntryDto entry, List<CepAlertDto> alerts, List<RecommendationDto> recommendations) {
}
