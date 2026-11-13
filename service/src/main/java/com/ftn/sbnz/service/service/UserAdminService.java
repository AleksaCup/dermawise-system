package com.ftn.sbnz.service.service;

import com.ftn.sbnz.service.dto.CreateUserRequest;
import com.ftn.sbnz.service.dto.UserDto;
import com.ftn.sbnz.service.entity.AppUser;
import com.ftn.sbnz.service.exception.BadRequestException;
import com.ftn.sbnz.service.exception.NotFoundException;
import com.ftn.sbnz.service.repository.AppUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class UserAdminService {

    private final AppUserRepository users;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public List<UserDto> list() {
        return users.findAll(Sort.by("id")).stream().map(UserDto::from).toList();
    }

    @Transactional
    public UserDto create(CreateUserRequest request) {
        String email = request.email().trim().toLowerCase(Locale.ROOT);
        if (users.existsByEmail(email)) {
            throw new BadRequestException("Nalog sa ovim email-om već postoji");
        }
        AppUser user = new AppUser(email, passwordEncoder.encode(request.password()), request.fullName().trim(),
                request.role());
        return UserDto.from(users.save(user));
    }

    @Transactional
    public UserDto setActive(Long id, boolean active, AppUser admin) {
        if (id.equals(admin.getId()) && !active) {
            throw new BadRequestException("Administrator ne može da deaktivira sopstveni nalog");
        }
        AppUser user = users.findById(id).orElseThrow(() -> new NotFoundException("Korisnik " + id + " ne postoji"));
        user.setActive(active);
        return UserDto.from(user);
    }
}
