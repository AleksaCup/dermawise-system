package com.ftn.sbnz.service.dto;

import java.util.List;

public record DemoWeekResponse(int entries, String note, List<CepAlertDto> alerts,
                               List<RecommendationDto> recommendations) {
}
