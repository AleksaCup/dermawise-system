package com.ftn.sbnz.model.questionnaire;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScoradAnswers {

    private String userId;

    private double affectedAreaPercent;

    private int erythema;
    private int edemaPapulation;
    private int excoriation;
    private int lichenification;
    private int oozingCrusting;
    private int dryness;

    private int pruritus;
    private int sleeplessness;

    public int getIntensitySum() {
        return erythema + edemaPapulation + excoriation + lichenification + oozingCrusting + dryness;
    }

    public int getSubjectiveSum() {
        return pruritus + sleeplessness;
    }
}
