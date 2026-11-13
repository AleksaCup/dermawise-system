package com.ftn.sbnz.service.repository;

import com.ftn.sbnz.model.enums.RecommendationStatus;
import com.ftn.sbnz.service.entity.AnalysisResultEntity;
import com.ftn.sbnz.service.entity.AppUser;
import com.ftn.sbnz.service.entity.RecommendationEntity;
import com.ftn.sbnz.service.entity.RecommendationOrigin;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RecommendationRepository extends JpaRepository<RecommendationEntity, Long> {

    List<RecommendationEntity> findByAnalysisOrderByIdAsc(AnalysisResultEntity analysis);

    List<RecommendationEntity> findByUserAndOriginOrderByCreatedAtDesc(AppUser user, RecommendationOrigin origin);

    List<RecommendationEntity> findByStatusOrderByCreatedAtAsc(RecommendationStatus status);

    long countByUserAndStatus(AppUser user, RecommendationStatus status);
}
