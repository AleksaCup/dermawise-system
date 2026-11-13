package com.ftn.sbnz.service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ftn.sbnz.service.ApiTestSupport;
import com.ftn.sbnz.service.repository.AppUserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class BackwardControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private AppUserRepository users;

    @Test
    void rosaceaHypothesisIsProvenThroughItsLeaves() throws Exception {
        String token = ApiTestSupport.registerUser(mvc, mapper);
        ApiTestSupport.submit(mvc, token, ApiTestSupport.questionnaire(mapper, "rosacea-ppr"));

        mvc.perform(get("/api/backward/hypotheses/rozaceja").header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.proven").value(true))
                .andExpect(jsonPath("$.query").value("imaRosaceju"))
                .andExpect(jsonPath("$.provenThrough", containsInAnyOrder("FLUSHING_UPITNIK",
                        "PAPULE_PUSTULE_BEZ_KOMEDONA", "PERZISTENTNI_ERITEM", "TELANGIEKTAZIJE")));

        mvc.perform(get("/api/backward/hypotheses/akne").header("Authorization", token))
                .andExpect(jsonPath("$.proven").value(false));
    }

    @Test
    void unknownGoalIs400() throws Exception {
        String token = ApiTestSupport.registerUser(mvc, mapper);
        ApiTestSupport.submit(mvc, token, ApiTestSupport.questionnaire(mapper, "rosacea-ppr"));

        mvc.perform(get("/api/backward/hypotheses/psorijaza").header("Authorization", token))
                .andExpect(status().isBadRequest());
    }

    @Test
    void dermatologistChecksPatientHypothesis() throws Exception {
        String token = ApiTestSupport.login(mvc, mapper, "dermatolog@dermawise.rs", "derm123");
        Long ana = users.findByEmail("ana@dermawise.rs").orElseThrow().getId();

        mvc.perform(get("/api/backward/hypotheses/akne").param("patientId", ana.toString())
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.proven").value(true))
                .andExpect(jsonPath("$.provenThrough[0]").value("KOMEDONI"));

        mvc.perform(get("/api/backward/hypotheses/akne").header("Authorization", token))
                .andExpect(status().isBadRequest());
    }

    @Test
    void allergenCheckShowsGroups() throws Exception {
        String token = ApiTestSupport.login(mvc, mapper, "ana@dermawise.rs", "user123");

        mvc.perform(get("/api/backward/allergen-check").param("sastojak", "Adapalen").header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sastojak").value("adapalen"))
                .andExpect(jsonPath("$.knownAllergen").value(false))
                .andExpect(jsonPath("$.groups[0]").value("retinoidi"));
    }
}
