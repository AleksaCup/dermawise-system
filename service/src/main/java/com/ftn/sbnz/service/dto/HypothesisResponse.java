package com.ftn.sbnz.service.dto;

import java.util.List;

public record HypothesisResponse(String goal, String query, boolean proven, List<String> provenThrough) {
}
