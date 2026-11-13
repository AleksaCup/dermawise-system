package com.ftn.sbnz.model.diary;

import com.ftn.sbnz.model.enums.FlushTrigger;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FlushingEpizoda {

    private String userId;
    private Date timestamp;
    private int trajanje;
    private FlushTrigger trigger;
}
