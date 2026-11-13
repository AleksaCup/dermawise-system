package com.ftn.sbnz.model.enums;

public enum FaceRegion {
    FOREHEAD(2),
    RIGHT_CHEEK(2),
    LEFT_CHEEK(2),
    NOSE(1),
    CHIN(1),
    CHEST_BACK(3);

    private final int weight;

    FaceRegion(int weight) {
        this.weight = weight;
    }

    public int getWeight() {
        return weight;
    }
}
