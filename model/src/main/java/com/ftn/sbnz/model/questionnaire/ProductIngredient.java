package com.ftn.sbnz.model.questionnaire;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductIngredient {

    private String name;
    private Double concentration;
}
