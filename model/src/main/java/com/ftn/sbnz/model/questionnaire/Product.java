package com.ftn.sbnz.model.questionnaire;

import com.ftn.sbnz.model.enums.BodyArea;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    private String name;
    private List<ProductIngredient> ingredients = new ArrayList<>();
    private Set<BodyArea> applicationAreas = new HashSet<>();
}
