package com.ftn.sbnz.model.enums;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

class FaceRegionTest {

    @Test
    void gagsWeightsSumToEleven() {
        int sum = Arrays.stream(FaceRegion.values()).mapToInt(FaceRegion::getWeight).sum();

        assertThat(sum).isEqualTo(11);
    }

    @Test
    void maxGagsScoreIs44() {
        int max = Arrays.stream(FaceRegion.values())
                .mapToInt(r -> r.getWeight() * LesionType.NODULES.getGrade())
                .sum();

        assertThat(max).isEqualTo(44);
    }
}
