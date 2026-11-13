package com.ftn.sbnz.service.controller;

import com.ftn.sbnz.service.dto.AllergenCheckResponse;
import com.ftn.sbnz.service.dto.ContactCause;
import com.ftn.sbnz.service.dto.HypothesisResponse;
import com.ftn.sbnz.service.security.SecurityUser;
import com.ftn.sbnz.service.service.BackwardService;
import com.ftn.sbnz.service.service.PatientService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/backward")
@RequiredArgsConstructor
public class BackwardController {

    private final BackwardService backwardService;
    private final PatientService patientService;

    @GetMapping("/hypotheses/{goal}")
    public HypothesisResponse hypothesis(@AuthenticationPrincipal SecurityUser principal,
                                         @PathVariable String goal,
                                         @RequestParam(required = false) Long patientId) {
        return backwardService.hypothesis(patientService.resolve(principal.getUser(), patientId), goal);
    }

    @GetMapping("/contact-dermatitis-cause")
    public List<ContactCause> contactDermatitisCause(@AuthenticationPrincipal SecurityUser principal,
                                                     @RequestParam(required = false) Long patientId) {
        return backwardService.contactDermatitisCauses(patientService.resolve(principal.getUser(), patientId));
    }

    @GetMapping("/allergen-check")
    public AllergenCheckResponse allergenCheck(@AuthenticationPrincipal SecurityUser principal,
                                               @RequestParam String sastojak,
                                               @RequestParam(required = false) Long patientId) {
        return backwardService.allergenCheck(patientService.resolve(principal.getUser(), patientId), sastojak);
    }
}
