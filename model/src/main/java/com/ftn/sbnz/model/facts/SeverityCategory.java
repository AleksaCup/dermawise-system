package com.ftn.sbnz.model.facts;

import com.ftn.sbnz.model.enums.Severity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SeverityCategory {

    private String userId;
    private String scale;
    private Severity severity;
}
