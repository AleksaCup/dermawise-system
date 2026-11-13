package com.ftn.sbnz.model.template;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MaxConcentrationByPhototype {

    private String sastojak;
    private int minFototip;
    private int maxFototip;
    private double maxKoncentracija;
    private String napomena;
}
