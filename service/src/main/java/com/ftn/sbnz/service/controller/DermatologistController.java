package com.ftn.sbnz.service.controller;

import com.ftn.sbnz.service.dto.AnalysisResponse;
import com.ftn.sbnz.service.dto.CepAlertDto;
import com.ftn.sbnz.service.dto.PatientSummary;
import com.ftn.sbnz.service.dto.PendingRecommendationDto;
import com.ftn.sbnz.service.dto.ProfileResponse;
import com.ftn.sbnz.service.dto.RecommendationDto;
import com.ftn.sbnz.service.dto.ReviewRequest;
import com.ftn.sbnz.service.security.SecurityUser;
import com.ftn.sbnz.service.service.AnalysisService;
import com.ftn.sbnz.service.service.CepService;
import com.ftn.sbnz.service.service.PatientService;
import com.ftn.sbnz.service.service.ProfileService;
import com.ftn.sbnz.service.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/dermatologist")
@RequiredArgsConstructor
public class DermatologistController {

    private final ReviewService reviewService;
    private final PatientService patientService;
    private final ProfileService profileService;
    private final AnalysisService analysisService;
    private final CepService cepService;

    @GetMapping("/patients")
    public List<PatientSummary> patients() {
        return reviewService.patients();
    }

    @GetMapping("/patients/{id}/profile")
    public ProfileResponse profile(@PathVariable Long id) {
        return profileService.get(patientService.require(id));
    }

    @GetMapping("/patients/{id}/analysis")
    public AnalysisResponse latestAnalysis(@PathVariable Long id) {
        return analysisService.latest(patientService.require(id));
    }

    @GetMapping("/patients/{id}/alerts")
    public List<CepAlertDto> alerts(@PathVariable Long id) {
        return cepService.alerts(patientService.require(id));
    }

    @GetMapping("/recommendations/pending")
    public List<PendingRecommendationDto> pending() {
        return reviewService.pending();
    }

    @PostMapping("/recommendations/{id}/approve")
    public RecommendationDto approve(@AuthenticationPrincipal SecurityUser principal, @PathVariable Long id,
                                     @Valid @RequestBody(required = false) ReviewRequest request) {
        return reviewService.approve(id, principal.getUser(), request == null ? null : request.comment());
    }

    @PostMapping("/recommendations/{id}/reject")
    public RecommendationDto reject(@AuthenticationPrincipal SecurityUser principal, @PathVariable Long id,
                                    @Valid @RequestBody(required = false) ReviewRequest request) {
        return reviewService.reject(id, principal.getUser(), request == null ? null : request.comment());
    }
}
