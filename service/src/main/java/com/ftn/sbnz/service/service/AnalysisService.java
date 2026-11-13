package com.ftn.sbnz.service.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ftn.sbnz.kjar.AnalysisRunner;
import com.ftn.sbnz.model.enums.RecommendationStatus;
import com.ftn.sbnz.model.enums.RoutineSlot;
import com.ftn.sbnz.model.facts.Diagnosis;
import com.ftn.sbnz.model.facts.Recommendation;
import com.ftn.sbnz.model.facts.User;
import com.ftn.sbnz.model.result.AnalysisResult;
import com.ftn.sbnz.service.config.KieBaseHolder;
import com.ftn.sbnz.service.dto.AnalysisResponse;
import com.ftn.sbnz.service.dto.AnalysisSummary;
import com.ftn.sbnz.service.dto.QuestionnaireRequest;
import com.ftn.sbnz.service.dto.RecommendationDto;
import com.ftn.sbnz.service.entity.AnalysisResultEntity;
import com.ftn.sbnz.service.entity.AppUser;
import com.ftn.sbnz.service.entity.RecommendationEntity;
import com.ftn.sbnz.service.entity.RecommendationOrigin;
import com.ftn.sbnz.service.entity.SkinProfile;
import com.ftn.sbnz.service.exception.NotFoundException;
import com.ftn.sbnz.service.repository.AnalysisResultRepository;
import com.ftn.sbnz.service.repository.RecommendationRepository;
import com.ftn.sbnz.service.repository.SkinProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AnalysisService {

    private static final Comparator<RecommendationEntity> BY_STEP =
            Comparator.comparing(RecommendationEntity::getStep).thenComparing(RecommendationEntity::getSastojak);

    private final KieBaseHolder kieBaseHolder;
    private final ProfileService profileService;
    private final SkinProfileRepository profiles;
    private final FactAssembler assembler;
    private final AnalysisResultRepository analyses;
    private final RecommendationRepository recommendations;
    private final ObjectMapper objectMapper;
    private final CepSessionManager cepSessionManager;
    private final AnalysisRunner runner = new AnalysisRunner();

    @Transactional
    public AnalysisResponse analyse(AppUser user) {
        SkinProfile profile = profileService.require(user);
        QuestionnaireRequest questionnaire = assembler.readQuestionnaire(profile);
        List<Object> facts = assembler.analysisFacts(user, questionnaire, Instant.now());

        AnalysisResult result = runner.run(kieBaseHolder.getAnalysisKieBase(), new User(user.factId()), facts);

        profile.setFototip(result.getFototip());
        profile.setMed(result.getMed());
        profiles.save(profile);
        cepSessionManager.invalidate(user.getId());

        AnalysisResultEntity entity = new AnalysisResultEntity();
        entity.setUser(user);
        entity.setFototip(result.getFototip());
        entity.setMed(result.getMed());
        entity.setResultJson(assembler.writeJson(result));
        AnalysisResultEntity saved = analyses.save(entity);

        List<Recommendation> all = new ArrayList<>(result.getMorningRoutine());
        all.addAll(result.getEveningRoutine());
        all.addAll(result.getWithdrawn());
        for (Recommendation recommendation : all) {
            recommendations.save(RecommendationMapper.toEntity(recommendation, user, saved, RecommendationOrigin.ANALYSIS));
        }
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public AnalysisResponse latest(AppUser user) {
        return analyses.findFirstByUserOrderByCreatedAtDesc(user)
                .map(this::toResponse)
                .orElseThrow(() -> new NotFoundException("Još nema analize za ovog korisnika"));
    }

    @Transactional(readOnly = true)
    public List<AnalysisSummary> history(AppUser user) {
        return analyses.findByUserOrderByCreatedAtDesc(user).stream()
                .map(e -> new AnalysisSummary(e.getId(), e.getCreatedAt(), e.getFototip(),
                        read(e).getDiagnoses().stream().map(Diagnosis::getCondition).toList()))
                .toList();
    }

    public AnalysisResponse toResponse(AnalysisResultEntity entity) {
        AnalysisResult result = read(entity);
        List<RecommendationEntity> stored = recommendations.findByAnalysisOrderByIdAsc(entity);
        return new AnalysisResponse(entity.getId(), entity.getCreatedAt(), entity.getFototip(), entity.getMed(),
                result.getDiagnoses(), result.getExcludedDiagnoses(),
                routine(stored, RoutineSlot.MORNING), routine(stored, RoutineSlot.EVENING),
                stored.stream().filter(r -> !inRoutine(r)).sorted(BY_STEP).map(RecommendationDto::from).toList(),
                result.getReferrals(), result.getSafetyNotes(), result.getContactDermatitisCauses(),
                result.getExplanation());
    }

    private List<RecommendationDto> routine(List<RecommendationEntity> stored, RoutineSlot slot) {
        return stored.stream()
                .filter(r -> r.getSlot() == slot && inRoutine(r))
                .sorted(BY_STEP)
                .map(RecommendationDto::from)
                .toList();
    }

    private static boolean inRoutine(RecommendationEntity r) {
        return r.getStatus() != RecommendationStatus.WITHDRAWN && r.getStatus() != RecommendationStatus.REJECTED;
    }

    private AnalysisResult read(AnalysisResultEntity entity) {
        try {
            return objectMapper.readValue(entity.getResultJson(), AnalysisResult.class);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Stored analysis cannot be read", e);
        }
    }
}
