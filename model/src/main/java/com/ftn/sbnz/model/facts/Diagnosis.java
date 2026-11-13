package com.ftn.sbnz.model.facts;

import com.ftn.sbnz.model.enums.Condition;
import com.ftn.sbnz.model.enums.RosaceaSubtype;
import com.ftn.sbnz.model.enums.Severity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Diagnosis {

    private String userId;
    private Condition condition;
    private Severity severity;
    private RosaceaSubtype subtype;
    private Double score;
}
