package com.ftn.sbnz.kjar;

public final class Thresholds {

    public static final int FLUSHING_PER_WEEK = 2;
    public static final int PPR_MIN_PAPULES = 5;
    public static final int PPR_MIN_PUSTULES = 3;

    public static final int CEP_FLUSHING_IN_7_DAYS = 4;
    public static final int CEP_DOMINANT_TRIGGER_IN_30_DAYS = 3;
    public static final double CEP_MED_FRACTION_PER_DAY = 0.5;
    public static final int CEP_UV_WARNINGS_IN_30_DAYS = 5;
    public static final double CEP_VERY_HIGH_UV_INDEX = 8;
    public static final int CEP_VERY_HIGH_UV_MINUTES = 30;
    public static final int CEP_SCALING_IN_14_DAYS = 3;

    private Thresholds() {
    }
}
