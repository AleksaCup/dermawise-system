package com.ftn.sbnz.model.facts;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.kie.api.definition.type.Position;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class IngredientGroup {

    @Position(0)
    private String name;
    @Position(1)
    private boolean crossReactive;
}
