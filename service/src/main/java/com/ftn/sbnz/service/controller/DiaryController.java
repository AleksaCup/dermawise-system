package com.ftn.sbnz.service.controller;

import com.ftn.sbnz.service.dto.DiaryEntryDto;
import com.ftn.sbnz.service.dto.DiaryEntryResponse;
import com.ftn.sbnz.service.dto.FlushingEntryRequest;
import com.ftn.sbnz.service.dto.ProductEntryRequest;
import com.ftn.sbnz.service.dto.SunEntryRequest;
import com.ftn.sbnz.service.dto.SymptomEntryRequest;
import com.ftn.sbnz.service.security.SecurityUser;
import com.ftn.sbnz.service.service.DiaryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/diary")
@RequiredArgsConstructor
public class DiaryController {

    private final DiaryService diaryService;

    @GetMapping
    public List<DiaryEntryDto> list(@AuthenticationPrincipal SecurityUser principal) {
        return diaryService.list(principal.getUser());
    }

    @PostMapping("/flushing")
    @ResponseStatus(HttpStatus.CREATED)
    public DiaryEntryResponse flushing(@AuthenticationPrincipal SecurityUser principal,
                                       @Valid @RequestBody FlushingEntryRequest request) {
        return diaryService.addFlushing(principal.getUser(), request);
    }

    @PostMapping("/sun")
    @ResponseStatus(HttpStatus.CREATED)
    public DiaryEntryResponse sun(@AuthenticationPrincipal SecurityUser principal,
                                  @Valid @RequestBody SunEntryRequest request) {
        return diaryService.addSun(principal.getUser(), request);
    }

    @PostMapping("/products")
    @ResponseStatus(HttpStatus.CREATED)
    public DiaryEntryResponse product(@AuthenticationPrincipal SecurityUser principal,
                                      @Valid @RequestBody ProductEntryRequest request) {
        return diaryService.addProduct(principal.getUser(), request);
    }

    @PostMapping("/symptoms")
    @ResponseStatus(HttpStatus.CREATED)
    public DiaryEntryResponse symptom(@AuthenticationPrincipal SecurityUser principal,
                                      @Valid @RequestBody SymptomEntryRequest request) {
        return diaryService.addSymptom(principal.getUser(), request);
    }
}
