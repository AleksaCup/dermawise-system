package com.ftn.sbnz.service.service;

import com.ftn.sbnz.model.enums.RecommendationStatus;
import com.ftn.sbnz.service.dto.PatientSummary;
import com.ftn.sbnz.service.dto.PendingRecommendationDto;
import com.ftn.sbnz.service.dto.RecommendationDto;
import com.ftn.sbnz.service.entity.AnalysisResultEntity;
import com.ftn.sbnz.service.entity.AppUser;
import com.ftn.sbnz.service.entity.RecommendationEntity;
import com.ftn.sbnz.service.entity.Role;
import com.ftn.sbnz.service.entity.SkinProfile;
import com.ftn.sbnz.service.exception.BadRequestException;
import com.ftn.sbnz.service.exception.NotFoundException;
import com.ftn.sbnz.service.repository.AnalysisResultRepository;
import com.ftn.sbnz.service.repository.AppUserRepository;
import com.ftn.sbnz.service.repository.RecommendationRepository;
import com.ftn.sbnz.service.repository.SkinProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final RecommendationRepository recommendations;
    private final AppUserRepository users;
    private final SkinProfileRepository profiles;
    private final AnalysisResultRepository analyses;

    @Transactional(readOnly = true)
    public List<PatientSummary> patients() {
        return users.findByRoleOrderByFullName(Role.USER).stream().map(this::summary).toList();
    }

    @Transactional(readOnly = true)
    public List<PendingRecommendationDto> pending() {
        return recommendations.findByStatusOrderByCreatedAtAsc(RecommendationStatus.PENDING_REVIEW).stream()
                .map(r -> new PendingRecommendationDto(r.getUser().getId(), r.getUser().getFullName(),
                        profiles.findByUser(r.getUser()).map(SkinProfile::isPregnant).orElse(false),
                        RecommendationDto.from(r)))
                .toList();
    }

    @Transactional
    public RecommendationDto approve(Long id, AppUser dermatologist, String comment) {
        return review(id, dermatologist, comment, RecommendationStatus.APPROVED);
    }

    @Transactional
    public RecommendationDto reject(Long id, AppUser dermatologist, String comment) {
        if (comment == null || comment.isBlank()) {
            throw new BadRequestException("Za odbijanje preporuke potreban je komentar");
        }
        return review(id, dermatologist, comment, RecommendationStatus.REJECTED);
    }

    private RecommendationDto review(Long id, AppUser dermatologist, String comment, RecommendationStatus status) {
        RecommendationEntity recommendation = recommendations.findById(id)
                .orElseThrow(() -> new NotFoundException("Preporuka " + id + " ne postoji"));
        if (recommendation.getStatus() != RecommendationStatus.PENDING_REVIEW) {
            throw new BadRequestException("Preporuka nije na čekanju, trenutni status je " + recommendation.getStatus());
        }
        recommendation.setStatus(status);
        recommendation.setReviewedBy(dermatologist);
        recommendation.setReviewedAt(Instant.now());
        recommendation.setReviewComment(comment == null ? null : comment.trim());
        return RecommendationDto.from(recommendation);
    }

    private PatientSummary summary(AppUser user) {
        SkinProfile profile = profiles.findByUser(user).orElse(null);
        Instant lastAnalysis = analyses.findFirstByUserOrderByCreatedAtDesc(user)
                .map(AnalysisResultEntity::getCreatedAt).orElse(null);
        return new PatientSummary(user.getId(), user.getFullName(), user.getEmail(), profile != null,
                profile == null ? 0 : profile.getFototip(), profile == null ? 0 : profile.getMed(),
                profile != null && profile.isPregnant(), lastAnalysis,
                recommendations.countByUserAndStatus(user, RecommendationStatus.PENDING_REVIEW));
    }
}
