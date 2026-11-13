package com.ftn.sbnz.service.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record RosaceaSection(
        boolean persistentErythema,
        boolean centrofacialDistribution,
        @Min(0) @Max(50) int flushingPerWeek,
        boolean telangiectasia,
        boolean papulesPustulesWithoutComedones,
        boolean ocularSymptoms,
        boolean phymatousChanges,
        @Min(0) @Max(3) int flushingGrade,
        @Min(0) @Max(3) int persistentErythemaGrade,
        @Min(0) @Max(3) int telangiectasiaGrade,
        @Min(0) @Max(3) int papulesPustulesGrade,
        @Min(0) int papuleCount,
        @Min(0) int pustuleCount,
        boolean perioralDistribution,
        boolean recentTopicalSteroid) {
}
