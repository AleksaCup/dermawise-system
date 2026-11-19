package com.ftn.sbnz.service.controller;

import com.ftn.sbnz.service.dto.ProfileResponse;
import com.ftn.sbnz.service.dto.QuestionnaireExample;
import com.ftn.sbnz.service.dto.QuestionnaireRequest;
import com.ftn.sbnz.service.security.SecurityUser;
import com.ftn.sbnz.service.service.ProfileService;
import com.ftn.sbnz.service.service.QuestionnaireExampleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;
    private final QuestionnaireExampleService exampleService;

    @GetMapping
    public ResponseEntity<ProfileResponse> get(@AuthenticationPrincipal SecurityUser principal) {
        return profileService.find(principal.getUser())
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    @PutMapping("/questionnaire")
    public ProfileResponse saveQuestionnaire(@AuthenticationPrincipal SecurityUser principal,
                                             @Valid @RequestBody QuestionnaireRequest request) {
        return profileService.saveQuestionnaire(principal.getUser(), request);
    }

    @GetMapping("/examples")
    public List<QuestionnaireExample> examples() {
        return exampleService.all();
    }
}
