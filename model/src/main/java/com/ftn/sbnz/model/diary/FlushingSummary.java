package com.ftn.sbnz.model.diary;

import com.ftn.sbnz.model.enums.FlushTrigger;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.kie.api.definition.type.Position;

import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FlushingSummary {

    @Position(0)
    private String userId;
    private int last7Days;
    private Set<FlushTrigger> triggers = new HashSet<>();
}
