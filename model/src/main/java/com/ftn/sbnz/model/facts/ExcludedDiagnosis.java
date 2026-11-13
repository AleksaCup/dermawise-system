package com.ftn.sbnz.model.facts;

import com.ftn.sbnz.model.enums.Condition;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExcludedDiagnosis {

    private String userId;
    private Condition condition;
    private String reason;
}
