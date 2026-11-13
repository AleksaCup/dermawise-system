package com.ftn.sbnz.service.service;

import com.ftn.sbnz.service.entity.AppUser;
import com.ftn.sbnz.service.entity.Role;
import com.ftn.sbnz.service.exception.BadRequestException;
import com.ftn.sbnz.service.exception.NotFoundException;
import com.ftn.sbnz.service.repository.AppUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PatientService {

    private final AppUserRepository users;

    public AppUser require(Long id) {
        return users.findById(id)
                .filter(user -> user.getRole() == Role.USER)
                .orElseThrow(() -> new NotFoundException("Pacijent " + id + " ne postoji"));
    }

    public AppUser resolve(AppUser caller, Long patientId) {
        if (caller.getRole() == Role.USER) {
            return caller;
        }
        if (patientId == null) {
            throw new BadRequestException("Dermatolog mora da navede patientId");
        }
        return require(patientId);
    }
}
