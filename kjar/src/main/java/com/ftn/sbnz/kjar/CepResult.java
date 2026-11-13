package com.ftn.sbnz.kjar;

import com.ftn.sbnz.model.cep.CepAlert;
import com.ftn.sbnz.model.facts.Recommendation;
import com.ftn.sbnz.model.facts.SafetyNote;

import java.util.List;

public record CepResult(List<CepAlert> alerts, List<Recommendation> recommendations, List<SafetyNote> notes) {

    public boolean isEmpty() {
        return alerts.isEmpty() && recommendations.isEmpty() && notes.isEmpty();
    }
}
