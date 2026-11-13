package com.ftn.sbnz.service.entity;

import com.ftn.sbnz.model.enums.Condition;
import com.ftn.sbnz.model.enums.RecommendationStatus;
import com.ftn.sbnz.model.enums.RoutineSlot;
import com.ftn.sbnz.model.enums.RoutineStep;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "recommendation")
@Getter
@Setter
@NoArgsConstructor
public class RecommendationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private AppUser user;

    @ManyToOne
    private AnalysisResultEntity analysis;

    @Enumerated(EnumType.STRING)
    private RecommendationOrigin origin;

    @Enumerated(EnumType.STRING)
    private Condition forCondition;

    private String sastojak;
    private Double koncentracija;
    private String form;

    @Enumerated(EnumType.STRING)
    private RoutineSlot slot;

    @Enumerated(EnumType.STRING)
    private RoutineStep step;

    private String frequency;
    private boolean requiresPrescription;

    @Enumerated(EnumType.STRING)
    private RecommendationStatus status;

    @Column(length = 1000)
    private String reason;
    private String source;
    private String ruleName;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "recommendation_warning", joinColumns = @JoinColumn(name = "recommendation_id"))
    @Column(name = "warning", length = 1000)
    private List<String> warnings = new ArrayList<>();

    @ManyToOne
    private AppUser reviewedBy;
    private Instant reviewedAt;
    @Column(length = 1000)
    private String reviewComment;

    private Instant createdAt = Instant.now();
}
