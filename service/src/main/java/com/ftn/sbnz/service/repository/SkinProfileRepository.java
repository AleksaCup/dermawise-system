package com.ftn.sbnz.service.repository;

import com.ftn.sbnz.service.entity.AppUser;
import com.ftn.sbnz.service.entity.SkinProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SkinProfileRepository extends JpaRepository<SkinProfile, Long> {

    Optional<SkinProfile> findByUser(AppUser user);
}
