package com.ftn.sbnz.kjar.seed;

import com.ftn.sbnz.model.facts.MEDValue;

import java.util.List;

public final class MedValues {

    private MedValues() {
    }

    public static List<MEDValue> all() {
        return List.of(
                new MEDValue(1, 20),
                new MEDValue(2, 30),
                new MEDValue(3, 40),
                new MEDValue(4, 50),
                new MEDValue(5, 75),
                new MEDValue(6, 120));
    }
}
