package com.ftn.sbnz.model.facts;

import com.ftn.sbnz.model.enums.Condition;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Referral {

    private String userId;
    private Condition forCondition;
    private String specialist;
    private String reason;
    private String source;
}
