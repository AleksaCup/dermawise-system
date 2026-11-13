package com.ftn.sbnz.service.dto;

import com.ftn.sbnz.service.entity.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateUserRequest(
        @NotBlank @Email String email,
        @NotBlank @Size(min = 6, message = "Lozinka mora imati bar 6 znakova") String password,
        @NotBlank String fullName,
        @NotNull Role role) {
}
