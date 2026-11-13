package com.ftn.sbnz.kjar;

import com.ftn.sbnz.kjar.seed.EvidenceGraph;
import com.ftn.sbnz.kjar.seed.IngredientGraph;
import org.kie.api.KieBase;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.rule.QueryResults;
import org.kie.api.runtime.rule.QueryResultsRow;

import java.util.ArrayList;
import java.util.List;

public class BackwardSession implements AutoCloseable {

    private static final KieBase BACKWARD = new KnowledgeBaseFactory().buildAll(List.of()).backward();

    private final KieSession session = BACKWARD.newKieSession();

    public BackwardSession() {
        EvidenceGraph.edges().forEach(session::insert);
        IngredientGraph.edges().forEach(session::insert);
        IngredientGraph.groups().forEach(session::insert);
    }

    public BackwardSession insertAll(List<?> facts) {
        facts.forEach(session::insert);
        return this;
    }

    public BackwardSession insert(Object fact) {
        session.insert(fact);
        return this;
    }

    public BackwardSession prepare() {
        session.getAgenda().getAgendaGroup("diagnosis").setFocus();
        session.getAgenda().getAgendaGroup("phototype").setFocus();
        session.fireAllRules();
        return this;
    }

    public List<String> column(String query, String column, Object... args) {
        List<String> values = new ArrayList<>();
        QueryResults results = session.getQueryResults(query, args);
        for (QueryResultsRow row : results) {
            values.add((String) row.get(column));
        }
        return values;
    }

    public int size(String query, Object... args) {
        return session.getQueryResults(query, args).size();
    }

    @Override
    public void close() {
        session.dispose();
    }
}
