package com.ftn.sbnz.model.cep;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HighUVExposureWarning {

    private String userId;
    private LocalDate day;
}
