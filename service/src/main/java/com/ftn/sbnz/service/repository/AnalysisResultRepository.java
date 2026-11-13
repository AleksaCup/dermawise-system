package com.ftn.sbnz.service.repository;

import com.ftn.sbnz.service.entity.AnalysisResultEntity;
import com.ftn.sbnz.service.entity.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AnalysisResultRepository extends JpaRepository<AnalysisResultEntity, Long> {

    Optional<AnalysisResultEntity> findFirstByUserOrderByCreatedAtDesc(AppUser user);

    List<AnalysisResultEntity> findByUserOrderByCreatedAtDesc(AppUser user);
}
