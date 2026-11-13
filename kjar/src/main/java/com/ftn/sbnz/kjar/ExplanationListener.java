package com.ftn.sbnz.kjar;

import com.ftn.sbnz.model.facts.ExplanationStep;
import org.drools.base.definitions.rule.impl.RuleImpl;
import org.kie.api.definition.rule.Rule;
import org.kie.api.event.rule.AfterMatchFiredEvent;
import org.kie.api.event.rule.DefaultAgendaEventListener;

import java.util.ArrayList;
import java.util.List;

public class ExplanationListener extends DefaultAgendaEventListener {

    private final List<ExplanationStep> steps = new ArrayList<>();

    @Override
    public void afterMatchFired(AfterMatchFiredEvent event) {
        Rule rule = event.getMatch().getRule();
        steps.add(new ExplanationStep(
                rule.getName(),
                ((RuleImpl) rule).getAgendaGroup(),
                metadata(rule, "source"),
                metadata(rule, "opis")));
    }

    public List<ExplanationStep> getSteps() {
        return List.copyOf(steps);
    }

    private static String metadata(Rule rule, String key) {
        Object value = rule.getMetaData().get(key);
        return value == null ? null : value.toString();
    }
}
