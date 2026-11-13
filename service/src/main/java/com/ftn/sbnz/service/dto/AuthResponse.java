package com.ftn.sbnz.service.dto;

import com.ftn.sbnz.service.entity.Role;

public record AuthResponse(String token, Long userId, String email, String fullName, Role role) {
}
