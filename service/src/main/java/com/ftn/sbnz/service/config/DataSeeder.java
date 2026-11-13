package com.ftn.sbnz.service.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.ftn.sbnz.kjar.template.MaxConcentrationRows;
import com.ftn.sbnz.model.template.MaxConcentrationByPhototype;
import com.ftn.sbnz.service.dto.QuestionnaireRequest;
import com.ftn.sbnz.service.entity.AppUser;
import com.ftn.sbnz.service.entity.MaxConcentrationRuleEntity;
import com.ftn.sbnz.service.entity.Role;
import com.ftn.sbnz.service.repository.AppUserRepository;
import com.ftn.sbnz.service.repository.MaxConcentrationRuleRepository;
import com.ftn.sbnz.service.service.ProfileService;
import com.ftn.sbnz.service.service.TemplateService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final AppUserRepository users;
    private final MaxConcentrationRuleRepository templateRules;
    private final PasswordEncoder passwordEncoder;
    private final ProfileService profileService;
    private final TemplateService templateService;
    private final ObjectMapper objectMapper;

    @Override
    public void run(String... args) throws Exception {
        if (users.count() == 0) {
            seedUsers();
        }
        if (templateRules.count() == 0) {
            seedTemplateRows();
        }
        templateService.rebuildFromDatabase();
    }

    private void seedUsers() throws IOException {
        user("admin@dermawise.rs", "admin123", "Administrator", Role.ADMIN);
        user("dermatolog@dermawise.rs", "derm123", "dr Jelena Marković", Role.DERMATOLOGIST);

        AppUser ana = user("ana@dermawise.rs", "user123", "Ana Petrović", Role.USER);
        profileService.saveQuestionnaire(ana, questionnaire(false));

        AppUser milica = user("milica@dermawise.rs", "user123", "Milica Jovanović", Role.USER);
        profileService.saveQuestionnaire(milica, questionnaire(true));

        log.info("Demo accounts created: admin, dermatolog, ana, milica");
    }

    private void seedTemplateRows() {
        for (MaxConcentrationByPhototype row : MaxConcentrationRows.load()) {
            MaxConcentrationRuleEntity entity = new MaxConcentrationRuleEntity();
            entity.setSastojak(row.getSastojak());
            entity.setMinFototip(row.getMinFototip());
            entity.setMaxFototip(row.getMaxFototip());
            entity.setMaxKoncentracija(row.getMaxKoncentracija());
            entity.setNapomena(row.getNapomena());
            entity.setUpdatedBy("seed");
            templateRules.save(entity);
        }
        log.info("MaxConcentrationByPhototype rows seeded from CSV");
    }

    private AppUser user(String email, String password, String fullName, Role role) {
        return users.save(new AppUser(email, passwordEncoder.encode(password), fullName, role));
    }

    private QuestionnaireRequest questionnaire(boolean pregnant) throws IOException {
        try (InputStream in = new ClassPathResource("questionnaires/moderate-acne.json").getInputStream()) {
            ObjectNode json = (ObjectNode) objectMapper.readTree(in);
            ((ObjectNode) json.get("lifestyle")).put("pregnant", pregnant);
            return objectMapper.treeToValue(json, QuestionnaireRequest.class);
        }
    }
}
