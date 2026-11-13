package com.ftn.sbnz.service.controller;

import com.ftn.sbnz.service.dto.TemplateRuleDto;
import com.ftn.sbnz.service.dto.TemplateRuleRequest;
import com.ftn.sbnz.service.security.SecurityUser;
import com.ftn.sbnz.service.service.TemplateService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/templates")
@RequiredArgsConstructor
public class TemplateController {

    private final TemplateService templateService;

    @GetMapping
    public List<TemplateRuleDto> list() {
        return templateService.list();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TemplateRuleDto create(@AuthenticationPrincipal SecurityUser principal,
                                  @Valid @RequestBody TemplateRuleRequest request) {
        return templateService.create(request, principal.getUser());
    }

    @PutMapping("/{id}")
    public TemplateRuleDto update(@AuthenticationPrincipal SecurityUser principal, @PathVariable Long id,
                                  @Valid @RequestBody TemplateRuleRequest request) {
        return templateService.update(id, request, principal.getUser());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        templateService.delete(id);
    }

    @GetMapping(value = "/generated-drl", produces = MediaType.TEXT_PLAIN_VALUE)
    public String generatedDrl() {
        return templateService.generatedDrl();
    }
}
