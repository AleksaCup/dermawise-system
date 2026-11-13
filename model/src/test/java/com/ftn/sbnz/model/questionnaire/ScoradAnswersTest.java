package com.ftn.sbnz.model.questionnaire;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ScoradAnswersTest {

    @Test
    void sumsAtMaximumMatchScoradLimits() {
        ScoradAnswers answers = new ScoradAnswers("u1", 100, 3, 3, 3, 3, 3, 3, 10, 10);

        assertThat(answers.getIntensitySum()).isEqualTo(18);
        assertThat(answers.getSubjectiveSum()).isEqualTo(20);
    }

    @Test
    void sumsAreZeroWithoutSymptoms() {
        ScoradAnswers answers = new ScoradAnswers();

        assertThat(answers.getIntensitySum()).isZero();
        assertThat(answers.getSubjectiveSum()).isZero();
    }
}
