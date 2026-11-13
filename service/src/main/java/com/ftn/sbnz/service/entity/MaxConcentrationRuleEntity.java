package com.ftn.sbnz.service.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "max_concentration_rule")
@Getter
@Setter
@NoArgsConstructor
public class MaxConcentrationRuleEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String sastojak;

    private int minFototip;
    private int maxFototip;
    private double maxKoncentracija;

    @Column(length = 500)
    private String napomena;

    private String updatedBy;
    private Instant updatedAt = Instant.now();
}
