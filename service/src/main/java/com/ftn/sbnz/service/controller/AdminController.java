package com.ftn.sbnz.service.controller;

import com.ftn.sbnz.service.dto.CreateUserRequest;
import com.ftn.sbnz.service.dto.KnowledgeBaseInfo;
import com.ftn.sbnz.service.dto.UserDto;
import com.ftn.sbnz.service.security.SecurityUser;
import com.ftn.sbnz.service.service.KnowledgeBaseInfoService;
import com.ftn.sbnz.service.service.UserAdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserAdminService userAdminService;
    private final KnowledgeBaseInfoService knowledgeBaseInfoService;

    @GetMapping("/users")
    public List<UserDto> users() {
        return userAdminService.list();
    }

    @PostMapping("/users")
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto create(@Valid @RequestBody CreateUserRequest request) {
        return userAdminService.create(request);
    }

    @PatchMapping("/users/{id}/active")
    public UserDto setActive(@AuthenticationPrincipal SecurityUser principal, @PathVariable Long id,
                             @RequestParam boolean active) {
        return userAdminService.setActive(id, active, principal.getUser());
    }

    @GetMapping("/knowledge-base")
    public KnowledgeBaseInfo knowledgeBase() {
        return knowledgeBaseInfoService.info();
    }
}
