package com.ftn.sbnz.service.repository;

import com.ftn.sbnz.service.entity.AppUser;
import com.ftn.sbnz.service.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {

    Optional<AppUser> findByEmail(String email);

    boolean existsByEmail(String email);

    List<AppUser> findByRoleOrderByFullName(Role role);
}
