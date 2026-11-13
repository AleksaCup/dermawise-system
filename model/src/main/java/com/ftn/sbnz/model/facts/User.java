package com.ftn.sbnz.model.facts;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.kie.api.definition.type.Position;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Position(0)
    private String id;
    private int fototip;
    private double med;
    private boolean pregnant;
    private boolean breastfeeding;

    public User(String id) {
        this.id = id;
    }
}
