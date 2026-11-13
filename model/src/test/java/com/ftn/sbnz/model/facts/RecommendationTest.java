package com.ftn.sbnz.model.facts;

import com.ftn.sbnz.model.enums.RecommendationStatus;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RecommendationTest {

    @Test
    void builderSetsDefaults() {
        Recommendation first = Recommendation.builder().sastojak("adapalen").koncentracija(0.1).build();
        Recommendation second = Recommendation.builder().sastojak("adapalen").koncentracija(0.1).build();

        assertThat(first.getStatus()).isEqualTo(RecommendationStatus.ACTIVE);
        assertThat(first.getWarnings()).isEmpty();
        assertThat(first.getId()).isNotEqualTo(second.getId());
    }

    @Test
    void addWarningKeepsAllWarnings() {
        Recommendation recommendation = Recommendation.builder().sastojak("retinol").build();

        recommendation.addWarning("prva");
        recommendation.addWarning("druga");

        assertThat(recommendation.getWarnings()).containsExactly("prva", "druga");
    }

    @Test
    void withdrawnAndRejectedAreNotActive() {
        Recommendation recommendation = Recommendation.builder().build();

        recommendation.setStatus(RecommendationStatus.PENDING_REVIEW);
        assertThat(recommendation.isActive()).isTrue();

        recommendation.setStatus(RecommendationStatus.WITHDRAWN);
        assertThat(recommendation.isActive()).isFalse();

        recommendation.setStatus(RecommendationStatus.REJECTED);
        assertThat(recommendation.isActive()).isFalse();
    }
}
