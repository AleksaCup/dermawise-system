package com.ftn.sbnz.service.dto;

import java.util.List;

public record AllergenCheckResponse(String sastojak, boolean knownAllergen, List<String> groups) {
}
