package com.ftn.sbnz.kjar;

import com.ftn.sbnz.kjar.seed.EvidenceGraph;
import com.ftn.sbnz.kjar.seed.IngredientGraph;
import org.kie.api.KieBase;
import org.kie.api.event.rule.AfterMatchFiredEvent;
import org.kie.api.event.rule.DefaultAgendaEventListener;
import org.kie.api.runtime.KieSession;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AnalysisSession implements AutoCloseable {

    private static final KieBase ANALYSIS = new KnowledgeBaseFactory().buildAll(List.of()).analysis();

    private final KieSession session;
    private final List<String> firedRules = new ArrayList<>();

    public AnalysisSession() {
        session = ANALYSIS.newKieSession();
        EvidenceGraph.edges().forEach(session::insert);
        IngredientGraph.edges().forEach(session::insert);
        IngredientGraph.groups().forEach(session::insert);
        session.addEventListener(new DefaultAgendaEventListener() {
            @Override
            public void afterMatchFired(AfterMatchFiredEvent event) {
                firedRules.add(event.getMatch().getRule().getName());
            }
        });
    }

    public AnalysisSession insert(Object... facts) {
        for (Object fact : facts) {
            session.insert(fact);
        }
        return this;
    }

    public AnalysisSession insertAll(List<?> facts) {
        facts.forEach(session::insert);
        return this;
    }

    public AnalysisSession fire() {
        session.getAgenda().getAgendaGroup("safety").setFocus();
        session.getAgenda().getAgendaGroup("therapy").setFocus();
        session.getAgenda().getAgendaGroup("diagnosis").setFocus();
        session.getAgenda().getAgendaGroup("phototype").setFocus();
        session.fireAllRules();
        return this;
    }

    public <T> List<T> all(Class<T> type) {
        return session.getObjects(type::isInstance).stream().map(type::cast).toList();
    }

    public int timesFired(String ruleName) {
        return Collections.frequency(firedRules, ruleName);
    }

    public List<String> firedRules() {
        return firedRules;
    }

    @Override
    public void close() {
        session.dispose();
    }
}
