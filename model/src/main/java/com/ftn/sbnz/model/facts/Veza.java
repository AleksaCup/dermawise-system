package com.ftn.sbnz.model.facts;

import com.ftn.sbnz.model.enums.VezaType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.kie.api.definition.type.Position;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Veza {

    @Position(0)
    private String fromNode;
    @Position(1)
    private String toNode;
    @Position(2)
    private VezaType type;
}
