package com.ftn.sbnz.model.questionnaire;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LifestyleAnswers {

    private String userId;
    private boolean pregnant;
    private boolean breastfeeding;
    private boolean hormonalTherapy;
    private List<String> allergies = new ArrayList<>();
    private List<Product> currentProducts = new ArrayList<>();
    private int dailySunMinutes;
}
