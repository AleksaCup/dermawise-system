package com.ftn.sbnz.kjar;

public class ThresholdRow {

    private final String scale;
    private final double limit;

    public ThresholdRow(String scale, double limit) {
        this.scale = scale;
        this.limit = limit;
    }

    public String getScale() {
        return scale;
    }

    public double getLimit() {
        return limit;
    }
}
