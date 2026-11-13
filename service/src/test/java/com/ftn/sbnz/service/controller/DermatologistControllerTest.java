package com.ftn.sbnz.service.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.ftn.sbnz.service.ApiTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class DermatologistControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    @Test
    void dermatologistApprovesPendingRecommendation() throws Exception {
        String user = ApiTestSupport.registerUser(mvc, mapper);
        ObjectNode questionnaire = ApiTestSupport.questionnaire(mapper, "moderate-acne");
        ((ObjectNode) questionnaire.get("lifestyle")).put("pregnant", true);
        ApiTestSupport.submit(mvc, user, questionnaire);
        JsonNode analysis = ApiTestSupport.analyse(mvc, mapper, user);
        long azelaicId = -1;
        for (JsonNode r : analysis.get("eveningRoutine")) {
            if (r.get("sastojak").asText().equals("azelainska kiselina")) {
                azelaicId = r.get("id").asLong();
                assertThat(r.get("status").asText()).isEqualTo("PENDING_REVIEW");
            }
        }
        assertThat(azelaicId).isPositive();

        String derm = ApiTestSupport.login(mvc, mapper, "dermatolog@dermawise.rs", "derm123");
        mvc.perform(get("/api/dermatologist/recommendations/pending").header("Authorization", derm))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.recommendation.id == " + azelaicId + ")].pregnant").value(true));

        mvc.perform(post("/api/dermatologist/recommendations/" + azelaicId + "/approve").header("Authorization", derm)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"comment\":\"Bezbedno u trudnoći\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));

        mvc.perform(post("/api/dermatologist/recommendations/" + azelaicId + "/approve").header("Authorization", derm))
                .andExpect(status().isBadRequest());

        mvc.perform(get("/api/analysis/latest").header("Authorization", user))
                .andExpect(jsonPath("$.eveningRoutine[?(@.sastojak == 'azelainska kiselina')].status").value("APPROVED"));
    }

    @Test
    void rejectingNeedsComment() throws Exception {
        String user = ApiTestSupport.registerUser(mvc, mapper);
        ApiTestSupport.submit(mvc, user, ApiTestSupport.questionnaire(mapper, "rosacea-ppr"));
        JsonNode analysis = ApiTestSupport.analyse(mvc, mapper, user);
        long ivermectin = -1;
        for (JsonNode r : analysis.get("eveningRoutine")) {
            if (r.get("sastojak").asText().equals("ivermektin")) {
                ivermectin = r.get("id").asLong();
            }
        }
        String derm = ApiTestSupport.login(mvc, mapper, "dermatolog@dermawise.rs", "derm123");

        mvc.perform(post("/api/dermatologist/recommendations/" + ivermectin + "/reject").header("Authorization", derm))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Za odbijanje preporuke potreban je komentar"));
        mvc.perform(post("/api/dermatologist/recommendations/" + ivermectin + "/reject").header("Authorization", derm)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"comment\":\"Prvo azelainska kiselina\"}"))
                .andExpect(jsonPath("$.status").value("REJECTED"));

        mvc.perform(get("/api/analysis/latest").header("Authorization", user))
                .andExpect(jsonPath("$.notInRoutine[?(@.sastojak == 'ivermektin')].reviewComment")
                        .value("Prvo azelainska kiselina"));
        assertThat(ivermectin).isPositive();
    }

    @Test
    void dermatologistSeesPatientsAndProfiles() throws Exception {
        String derm = ApiTestSupport.login(mvc, mapper, "dermatolog@dermawise.rs", "derm123");

        String body = mvc.perform(get("/api/dermatologist/patients").header("Authorization", derm))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        JsonNode milica = null;
        for (JsonNode p : mapper.readTree(body)) {
            if (p.get("email").asText().equals("milica@dermawise.rs")) {
                milica = p;
            }
        }
        assertThat(milica).isNotNull();
        assertThat(milica.get("pregnant").asBoolean()).isTrue();

        mvc.perform(get("/api/dermatologist/patients/" + milica.get("id").asLong() + "/profile")
                        .header("Authorization", derm))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.questionnaire.lifestyle.pregnant").value(true));
        mvc.perform(get("/api/dermatologist/patients/999999/profile").header("Authorization", derm))
                .andExpect(status().isNotFound());
    }
}
