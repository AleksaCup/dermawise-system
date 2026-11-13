package com.ftn.sbnz.service.dto;

import com.ftn.sbnz.service.entity.AppUser;
import com.ftn.sbnz.service.entity.Role;

import java.time.Instant;

public record UserDto(Long id, String email, String fullName, Role role, boolean active, Instant createdAt) {

    public static UserDto from(AppUser user) {
        return new UserDto(user.getId(), user.getEmail(), user.getFullName(), user.getRole(), user.isActive(),
                user.getCreatedAt());
    }
}
