package com.ftn.sbnz.service.entity;

import com.ftn.sbnz.model.enums.BodyArea;
import com.ftn.sbnz.model.enums.FlushTrigger;
import com.ftn.sbnz.model.enums.SymptomType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class DiaryEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private AppUser user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DiaryEntryType type;

    @Column(nullable = false)
    private Instant occurredAt;

    private Integer durationMinutes;
    @Enumerated(EnumType.STRING)
    private FlushTrigger flushTrigger;

    private Double uvIndex;
    private Integer minutesInSun;
    private Integer sunscreenSpf;

    private String productName;
    @Column(length = 10000)
    private String productJson;

    @Enumerated(EnumType.STRING)
    private SymptomType symptomType;
    @Enumerated(EnumType.STRING)
    private BodyArea symptomLocation;
}
