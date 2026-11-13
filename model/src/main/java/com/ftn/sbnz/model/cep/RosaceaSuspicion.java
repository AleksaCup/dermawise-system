package com.ftn.sbnz.model.cep;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RosaceaSuspicion {

    private String userId;
    private String subtype;
    private String level;
}
