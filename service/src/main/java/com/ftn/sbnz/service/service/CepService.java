package com.ftn.sbnz.service.service;

import com.ftn.sbnz.kjar.CepResult;
import com.ftn.sbnz.model.cep.CepAlert;
import com.ftn.sbnz.model.enums.CepAlertType;
import com.ftn.sbnz.model.facts.Recommendation;
import com.ftn.sbnz.model.facts.SafetyNote;
import com.ftn.sbnz.service.dto.CepAlertDto;
import com.ftn.sbnz.service.dto.DiaryEntryDto;
import com.ftn.sbnz.service.dto.DiaryEntryResponse;
import com.ftn.sbnz.service.dto.RecommendationDto;
import com.ftn.sbnz.service.entity.AppUser;
import com.ftn.sbnz.service.entity.CepAlertEntity;
import com.ftn.sbnz.service.entity.DiaryEntry;
import com.ftn.sbnz.service.entity.RecommendationOrigin;
import com.ftn.sbnz.service.exception.NotFoundException;
import com.ftn.sbnz.service.repository.CepAlertRepository;
import com.ftn.sbnz.service.repository.RecommendationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CepService {

    private final CepSessionManager sessionManager;
    private final CepAlertRepository alerts;
    private final RecommendationRepository recommendations;

    @Transactional
    public DiaryEntryResponse process(AppUser user, DiaryEntry entry) {
        CepResult result = sessionManager.submit(user, entry);

        List<CepAlertDto> savedAlerts = new ArrayList<>();
        for (CepAlert alert : result.alerts()) {
            savedAlerts.add(save(user, alert.getType(), alert.getMessage(), alert.getTimestamp().toInstant()));
        }
        for (SafetyNote note : result.notes()) {
            savedAlerts.add(save(user, CepAlertType.SAFETY_NOTE, note.getText(), entry.getOccurredAt()));
        }
        List<RecommendationDto> savedRecommendations = new ArrayList<>();
        for (Recommendation recommendation : result.recommendations()) {
            savedRecommendations.add(RecommendationDto.from(recommendations.save(
                    RecommendationMapper.toEntity(recommendation, user, null, RecommendationOrigin.CEP))));
        }
        return new DiaryEntryResponse(DiaryEntryDto.from(entry), savedAlerts, savedRecommendations);
    }

    @Transactional(readOnly = true)
    public List<CepAlertDto> alerts(AppUser user) {
        return alerts.findByUserOrderByOccurredAtDesc(user).stream().map(CepAlertDto::from).toList();
    }

    @Transactional(readOnly = true)
    public List<RecommendationDto> recommendations(AppUser user) {
        return recommendations.findByUserAndOriginOrderByCreatedAtDesc(user, RecommendationOrigin.CEP).stream()
                .map(RecommendationDto::from)
                .toList();
    }

    @Transactional
    public CepAlertDto markSeen(AppUser user, Long alertId) {
        CepAlertEntity alert = alerts.findById(alertId)
                .filter(a -> a.getUser().getId().equals(user.getId()))
                .orElseThrow(() -> new NotFoundException("Upozorenje " + alertId + " ne postoji"));
        alert.setSeen(true);
        return CepAlertDto.from(alert);
    }

    private CepAlertDto save(AppUser user, CepAlertType type, String message, Instant occurredAt) {
        CepAlertEntity entity = new CepAlertEntity();
        entity.setUser(user);
        entity.setType(type);
        entity.setMessage(message);
        entity.setOccurredAt(occurredAt);
        return CepAlertDto.from(alerts.save(entity));
    }
}
