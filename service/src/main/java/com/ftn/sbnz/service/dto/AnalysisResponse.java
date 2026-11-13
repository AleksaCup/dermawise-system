package com.ftn.sbnz.service.dto;

import com.ftn.sbnz.model.facts.Diagnosis;
import com.ftn.sbnz.model.facts.ExcludedDiagnosis;
import com.ftn.sbnz.model.facts.ExplanationStep;
import com.ftn.sbnz.model.facts.Referral;
import com.ftn.sbnz.model.facts.SafetyNote;

import java.time.Instant;
import java.util.List;

public record AnalysisResponse(
        Long id,
        Instant createdAt,
        int fototip,
        double med,
        List<Diagnosis> diagnoses,
        List<ExcludedDiagnosis> excludedDiagnoses,
        List<RecommendationDto> morningRoutine,
        List<RecommendationDto> eveningRoutine,
        List<RecommendationDto> notInRoutine,
        List<Referral> referrals,
        List<SafetyNote> safetyNotes,
        List<String> contactDermatitisCauses,
        List<ExplanationStep> explanation) {
}
