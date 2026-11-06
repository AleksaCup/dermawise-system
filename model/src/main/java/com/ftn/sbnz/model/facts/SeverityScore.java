package com.ftn.sbnz.model.facts;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SeverityScore {

    private String userId;
    private String scale;
    private double value;
}
