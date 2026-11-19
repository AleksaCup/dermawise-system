package com.ftn.sbnz.service.dto;

import java.util.List;

public record IngredientOption(String name, Kind kind, List<String> partOf) {

    public enum Kind {
        THERAPY, INGREDIENT, GROUP, MIXTURE
    }
}
