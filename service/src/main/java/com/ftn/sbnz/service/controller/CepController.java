package com.ftn.sbnz.service.controller;

import com.ftn.sbnz.service.dto.CepAlertDto;
import com.ftn.sbnz.service.security.SecurityUser;
import com.ftn.sbnz.service.service.CepService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/cep/alerts")
@RequiredArgsConstructor
public class CepController {

    private final CepService cepService;

    @GetMapping
    public List<CepAlertDto> alerts(@AuthenticationPrincipal SecurityUser principal) {
        return cepService.alerts(principal.getUser());
    }

    @PostMapping("/{id}/seen")
    public CepAlertDto markSeen(@AuthenticationPrincipal SecurityUser principal, @PathVariable Long id) {
        return cepService.markSeen(principal.getUser(), id);
    }
}
