package com.ftn.sbnz.service.service;

import com.ftn.sbnz.kjar.seed.SeedFacts;
import com.ftn.sbnz.model.facts.User;
import com.ftn.sbnz.model.questionnaire.Product;
import com.ftn.sbnz.service.config.KieBaseHolder;
import com.ftn.sbnz.service.dto.AllergenCheckResponse;
import com.ftn.sbnz.service.dto.ContactCause;
import com.ftn.sbnz.service.dto.HypothesisResponse;
import com.ftn.sbnz.service.entity.AppUser;
import com.ftn.sbnz.service.entity.SkinProfile;
import com.ftn.sbnz.service.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.rule.QueryResults;
import org.kie.api.runtime.rule.QueryResultsRow;
import org.kie.api.runtime.rule.Variable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class BackwardService {

    public static final Map<String, String> GOALS = Map.of(
            "rozaceja", "imaRosaceju",
            "akne", "imaAkne",
            "perioralni-dermatitis", "imaPerioralniDermatitis");

    private final KieBaseHolder kieBaseHolder;
    private final ProfileService profileService;
    private final FactAssembler assembler;

    @Transactional(readOnly = true)
    public HypothesisResponse hypothesis(AppUser patient, String goal) {
        String query = GOALS.get(goal);
        if (query == null) {
            throw new BadRequestException("Nepoznat cilj " + goal + ", dozvoljeno: " + new TreeSet<>(GOALS.keySet()));
        }
        return withSession(patient, session -> {
            QueryResults results = session.getQueryResults(query, patient.factId());
            Set<String> leaves = new TreeSet<>();
            for (QueryResultsRow row : results) {
                leaves.add((String) row.get("$znak"));
            }
            return new HypothesisResponse(goal, query, results.size() > 0, List.copyOf(leaves));
        });
    }

    @Transactional(readOnly = true)
    public List<ContactCause> contactDermatitisCauses(AppUser patient) {
        return withSession(patient, session -> {
            Set<ContactCause> causes = new LinkedHashSet<>();
            QueryResults results = session.getQueryResults("uzrocnikKontaktnogDermatitisa", patient.factId(),
                    Variable.v);
            for (QueryResultsRow row : results) {
                Product product = (Product) row.get("$proizvod");
                causes.add(new ContactCause((String) row.get("sastojak"), product.getName()));
            }
            return List.copyOf(causes);
        });
    }

    @Transactional(readOnly = true)
    public AllergenCheckResponse allergenCheck(AppUser patient, String ingredient) {
        String sastojak = ProfileService.name(ingredient);
        return withSession(patient, session -> {
            boolean known = session.getQueryResults("PoznatAlergen", patient.factId(), sastojak).size() > 0;
            Set<String> groups = new TreeSet<>();
            for (QueryResultsRow row : session.getQueryResults("pripada", sastojak, Variable.v)) {
                groups.add((String) row.get("grupa"));
            }
            return new AllergenCheckResponse(sastojak, known, List.copyOf(groups));
        });
    }

    private <T> T withSession(AppUser patient, Function<KieSession, T> work) {
        SkinProfile profile = profileService.require(patient);
        List<Object> facts = assembler.analysisFacts(patient, assembler.readQuestionnaire(profile), Instant.now());

        KieSession session = kieBaseHolder.getBackwardKieBase().newKieSession();
        try {
            SeedFacts.all().forEach(session::insert);
            session.insert(new User(patient.factId()));
            facts.forEach(session::insert);
            session.getAgenda().getAgendaGroup("diagnosis").setFocus();
            session.getAgenda().getAgendaGroup("phototype").setFocus();
            session.fireAllRules();
            return work.apply(session);
        } finally {
            session.dispose();
        }
    }
}
