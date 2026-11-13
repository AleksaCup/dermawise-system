package com.ftn.sbnz.service.controller;

import com.ftn.sbnz.service.dto.AnalysisResponse;
import com.ftn.sbnz.service.dto.AnalysisSummary;
import com.ftn.sbnz.service.security.SecurityUser;
import com.ftn.sbnz.service.service.AnalysisService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/analysis")
@RequiredArgsConstructor
public class AnalysisController {

    private final AnalysisService analysisService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AnalysisResponse analyse(@AuthenticationPrincipal SecurityUser principal) {
        return analysisService.analyse(principal.getUser());
    }

    @GetMapping("/latest")
    public AnalysisResponse latest(@AuthenticationPrincipal SecurityUser principal) {
        return analysisService.latest(principal.getUser());
    }

    @GetMapping
    public List<AnalysisSummary> history(@AuthenticationPrincipal SecurityUser principal) {
        return analysisService.history(principal.getUser());
    }
}
