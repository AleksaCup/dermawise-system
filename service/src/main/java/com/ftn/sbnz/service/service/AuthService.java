package com.ftn.sbnz.service.service;

import com.ftn.sbnz.service.dto.AuthResponse;
import com.ftn.sbnz.service.dto.LoginRequest;
import com.ftn.sbnz.service.dto.RegisterRequest;
import com.ftn.sbnz.service.entity.AppUser;
import com.ftn.sbnz.service.entity.Role;
import com.ftn.sbnz.service.exception.BadRequestException;
import com.ftn.sbnz.service.repository.AppUserRepository;
import com.ftn.sbnz.service.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AppUserRepository users;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String email = request.email().trim().toLowerCase(Locale.ROOT);
        if (users.existsByEmail(email)) {
            throw new BadRequestException("Nalog sa ovim email-om već postoji");
        }
        AppUser user = users.save(new AppUser(email, passwordEncoder.encode(request.password()),
                request.fullName().trim(), Role.USER));
        return response(user);
    }

    public AuthResponse login(LoginRequest request) {
        AppUser user = users.findByEmail(request.email().trim().toLowerCase(Locale.ROOT))
                .filter(u -> passwordEncoder.matches(request.password(), u.getPasswordHash()))
                .orElseThrow(() -> new BadRequestException("Pogrešan email ili lozinka"));
        if (!user.isActive()) {
            throw new BadRequestException("Nalog je deaktiviran");
        }
        return response(user);
    }

    private AuthResponse response(AppUser user) {
        return new AuthResponse(jwtService.generate(user), user.getId(), user.getEmail(), user.getFullName(),
                user.getRole());
    }
}
