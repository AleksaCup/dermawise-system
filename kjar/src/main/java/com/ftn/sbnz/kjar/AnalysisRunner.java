package com.ftn.sbnz.kjar;

import com.ftn.sbnz.kjar.seed.SeedFacts;
import com.ftn.sbnz.model.enums.RecommendationStatus;
import com.ftn.sbnz.model.enums.RoutineSlot;
import com.ftn.sbnz.model.facts.Diagnosis;
import com.ftn.sbnz.model.facts.ExcludedDiagnosis;
import com.ftn.sbnz.model.facts.Recommendation;
import com.ftn.sbnz.model.facts.Referral;
import com.ftn.sbnz.model.facts.SafetyNote;
import com.ftn.sbnz.model.facts.User;
import com.ftn.sbnz.model.result.AnalysisResult;
import org.kie.api.KieBase;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.rule.QueryResultsRow;
import org.kie.api.runtime.rule.Variable;

import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;

public class AnalysisRunner {

    public static final List<String> FOCUS_ORDER = List.of("safety", "therapy", "diagnosis", "phototype");

    private static final Comparator<Recommendation> BY_STEP =
            Comparator.comparing(Recommendation::getStep).thenComparing(Recommendation::getSastojak);

    public AnalysisResult run(KieBase kieBase, User user, List<?> facts) {
        KieSession session = kieBase.newKieSession();
        ExplanationListener listener = new ExplanationListener();
        session.addEventListener(listener);
        try {
            SeedFacts.all().forEach(session::insert);
            session.insert(user);
            facts.forEach(session::insert);
            for (String group : FOCUS_ORDER) {
                session.getAgenda().getAgendaGroup(group).setFocus();
            }
            session.fireAllRules();

            AnalysisResult result = collect(session, user);
            result.setExplanation(listener.getSteps());
            return result;
        } finally {
            session.dispose();
        }
    }

    private AnalysisResult collect(KieSession session, User user) {
        String userId = user.getId();
        AnalysisResult result = new AnalysisResult();
        result.setUserId(userId);
        result.setFototip(user.getFototip());
        result.setMed(user.getMed());

        result.setDiagnoses(objects(session, Diagnosis.class).stream()
                .filter(d -> userId.equals(d.getUserId())).toList());
        result.setExcludedDiagnoses(objects(session, ExcludedDiagnosis.class).stream()
                .filter(e -> userId.equals(e.getUserId())).toList());
        result.setReferrals(objects(session, Referral.class).stream()
                .filter(r -> userId.equals(r.getUserId())).toList());
        result.setSafetyNotes(objects(session, SafetyNote.class).stream()
                .filter(n -> userId.equals(n.getUserId())).toList());

        List<Recommendation> recommendations = objects(session, Recommendation.class).stream()
                .filter(r -> userId.equals(r.getUserId())).toList();
        result.setMorningRoutine(routine(recommendations, RoutineSlot.MORNING));
        result.setEveningRoutine(routine(recommendations, RoutineSlot.EVENING));
        result.setWithdrawn(recommendations.stream()
                .filter(r -> r.getStatus() == RecommendationStatus.WITHDRAWN)
                .sorted(BY_STEP).toList());

        TreeSet<String> causes = new TreeSet<>();
        for (QueryResultsRow row : session.getQueryResults("uzrocnikKontaktnogDermatitisa", userId, Variable.v)) {
            causes.add((String) row.get("sastojak"));
        }
        result.setContactDermatitisCauses(List.copyOf(causes));
        return result;
    }

    private static List<Recommendation> routine(List<Recommendation> recommendations, RoutineSlot slot) {
        return recommendations.stream()
                .filter(r -> r.getSlot() == slot)
                .filter(r -> r.getStatus() != RecommendationStatus.WITHDRAWN
                        && r.getStatus() != RecommendationStatus.REJECTED)
                .sorted(BY_STEP)
                .toList();
    }

    private static <T> List<T> objects(KieSession session, Class<T> type) {
        return session.getObjects(type::isInstance).stream().map(type::cast).toList();
    }
}
