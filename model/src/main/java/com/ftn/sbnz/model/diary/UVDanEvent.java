package com.ftn.sbnz.model.diary;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UVDanEvent {

    private String userId;
    private Date date;
    private double uvIndex;
    private int minutiNaSuncu;
    private int sunscreenSPF;
}
