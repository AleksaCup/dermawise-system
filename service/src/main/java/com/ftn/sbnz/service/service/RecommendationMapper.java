package com.ftn.sbnz.service.service;

import com.ftn.sbnz.model.facts.Recommendation;
import com.ftn.sbnz.service.entity.AnalysisResultEntity;
import com.ftn.sbnz.service.entity.AppUser;
import com.ftn.sbnz.service.entity.RecommendationEntity;
import com.ftn.sbnz.service.entity.RecommendationOrigin;

import java.util.ArrayList;

public final class RecommendationMapper {

    private RecommendationMapper() {
    }

    public static RecommendationEntity toEntity(Recommendation r, AppUser user, AnalysisResultEntity analysis,
                                                RecommendationOrigin origin) {
        RecommendationEntity e = new RecommendationEntity();
        e.setUser(user);
        e.setAnalysis(analysis);
        e.setOrigin(origin);
        e.setForCondition(r.getForCondition());
        e.setSastojak(r.getSastojak());
        e.setKoncentracija(r.getKoncentracija());
        e.setForm(r.getForm());
        e.setSlot(r.getSlot());
        e.setStep(r.getStep());
        e.setFrequency(r.getFrequency());
        e.setRequiresPrescription(r.isRequiresPrescription());
        e.setStatus(r.getStatus());
        e.setReason(r.getReason());
        e.setSource(r.getSource());
        e.setRuleName(r.getRuleName());
        e.setWarnings(new ArrayList<>(r.getWarnings()));
        return e;
    }
}
