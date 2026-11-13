package com.ftn.sbnz.kjar.seed;

import java.util.ArrayList;
import java.util.List;

public final class SeedFacts {

    private SeedFacts() {
    }

    public static List<Object> all() {
        List<Object> facts = new ArrayList<>();
        facts.addAll(EvidenceGraph.edges());
        facts.addAll(IngredientGraph.edges());
        facts.addAll(IngredientGraph.groups());
        facts.addAll(MedValues.all());
        return facts;
    }
}
