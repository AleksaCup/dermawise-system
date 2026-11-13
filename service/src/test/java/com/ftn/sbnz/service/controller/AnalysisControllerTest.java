package com.ftn.sbnz.service.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.ftn.sbnz.service.ApiTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AnalysisControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    @Test
    void moderateAcneAnalysisThroughTheApi() throws Exception {
        String token = ApiTestSupport.registerUser(mvc, mapper);
        ApiTestSupport.submit(mvc, token, ApiTestSupport.questionnaire(mapper, "moderate-acne"))
                .andExpect(status().isOk());

        JsonNode result = ApiTestSupport.analyse(mvc, mapper, token);

        assertThat(result.get("fototip").asInt()).isEqualTo(2);
        assertThat(result.get("med").asDouble()).isEqualTo(30.0);
        assertThat(result.at("/diagnoses/0/condition").asText()).isEqualTo("ACNE");
        assertThat(result.at("/diagnoses/0/severity").asText()).isEqualTo("MODERATE");
        assertThat(names(result.get("morningRoutine"))).contains("benzoil peroksid", "SPF 50");
        assertThat(names(result.get("eveningRoutine"))).contains("adapalen");
        assertThat(result.at("/morningRoutine/0/id").asLong()).isPositive();
        assertThat(result.get("explanation").size()).isGreaterThan(5);

        mvc.perform(get("/api/analysis/latest").header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(result.get("id").asLong()));
        mvc.perform(get("/api/profile").header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fototip").value(2));
    }

    @Test
    void pregnantUserGetsAzelaicAcidInsteadOfAdapalene() throws Exception {
        String token = ApiTestSupport.registerUser(mvc, mapper);
        ObjectNode questionnaire = ApiTestSupport.questionnaire(mapper, "moderate-acne");
        ((ObjectNode) questionnaire.get("lifestyle")).put("pregnant", true);
        ApiTestSupport.submit(mvc, token, questionnaire).andExpect(status().isOk());

        JsonNode result = ApiTestSupport.analyse(mvc, mapper, token);

        assertThat(names(result.get("eveningRoutine"))).contains("azelainska kiselina").doesNotContain("adapalen");
        assertThat(names(result.get("notInRoutine"))).containsExactly("adapalen");
        assertThat(result.at("/notInRoutine/0/status").asText()).isEqualTo("WITHDRAWN");
    }

    @Test
    void analysisWithoutQuestionnaireIs404() throws Exception {
        String token = ApiTestSupport.registerUser(mvc, mapper);

        mvc.perform(post("/api/analysis").header("Authorization", token))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Korisnik još nije popunio upitnik"));
    }

    @Test
    void invalidQuestionnaireIs400() throws Exception {
        String token = ApiTestSupport.registerUser(mvc, mapper);
        ObjectNode questionnaire = ApiTestSupport.questionnaire(mapper, "moderate-acne");
        ((ObjectNode) questionnaire.get("rosacea")).put("flushingGrade", 7);
        ((ObjectNode) questionnaire.get("lesions")).remove("CHIN");

        ApiTestSupport.submit(mvc, token, questionnaire)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors['rosacea.flushingGrade']").exists());

        ((ObjectNode) questionnaire.get("rosacea")).put("flushingGrade", 1);
        ApiTestSupport.submit(mvc, token, questionnaire)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Nedostaje lezija za region CHIN"));
    }

    static List<String> names(JsonNode routine) {
        List<String> names = new ArrayList<>();
        routine.forEach(r -> names.add(r.get("sastojak").asText()));
        return names;
    }
}
