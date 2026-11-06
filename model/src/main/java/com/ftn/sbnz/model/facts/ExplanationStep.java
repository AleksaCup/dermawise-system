package com.ftn.sbnz.model.facts;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExplanationStep {

    private String ruleName;
    private String agendaGroup;
    private String source;
    private String description;
}
