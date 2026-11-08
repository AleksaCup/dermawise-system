package com.ftn.sbnz.model.cep;

import com.ftn.sbnz.model.enums.FlushTrigger;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DominantTrigger {

    private String userId;
    private FlushTrigger trigger;
}
