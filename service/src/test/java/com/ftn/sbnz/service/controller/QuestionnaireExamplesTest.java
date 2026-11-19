package com.ftn.sbnz.service.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ftn.sbnz.service.ApiTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@SpringBootTest
@AutoConfigureMockMvc
class QuestionnaireExamplesTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    @Test
    void everyExampleGivesItsDiagnosis() throws Exception {
        String token = ApiTestSupport.registerUser(mvc, mapper);
        JsonNode examples = mapper.readTree(mvc.perform(get("/api/profile/examples").header("Authorization", token))
                .andReturn().getResponse().getContentAsString());
        assertThat(examples).hasSize(7);

        Map<String, JsonNode> results = new HashMap<>();
        for (JsonNode example : examples) {
            String user = ApiTestSupport.registerUser(mvc, mapper);
            ApiTestSupport.submit(mvc, user, example.get("questionnaire"));
            results.put(example.get("key").asText(), ApiTestSupport.analyse(mvc, mapper, user));
        }

        assertThat(diagnoses(results.get("moderate-acne"))).containsExactly("ACNE MODERATE");
        assertThat(results.get("moderate-acne").get("fototip").asInt()).isEqualTo(2);

        assertThat(statuses(results.get("moderate-acne-pregnant"))).contains("adapalen WITHDRAWN",
                "azelainska kiselina PENDING_REVIEW");

        assertThat(results.get("acne-dark-skin").get("fototip").asInt()).isEqualTo(5);
        assertThat(statuses(results.get("acne-dark-skin"))).contains("azelainska kiselina PENDING_REVIEW");

        assertThat(diagnoses(results.get("rosacea-ppr"))).containsExactly("ROSACEA MODERATE");
        assertThat(diagnoses(results.get("perioral-dermatitis"))).containsExactly("PERIORAL_DERMATITIS");

        assertThat(diagnoses(results.get("atopic-dermatitis"))).containsExactly("ATOPIC_DERMATITIS MODERATE");
        assertThat(results.get("atopic-dermatitis").at("/diagnoses/0/score").asDouble()).isEqualTo(38.5);

        assertThat(results.get("known-allergen").get("safetyNotes").toString())
                .contains("sadrži methylisothiazolinone, na koji ste alergični");
    }

    private List<String> diagnoses(JsonNode result) {
        List<String> list = new ArrayList<>();
        for (JsonNode d : result.get("diagnoses")) {
            String severity = d.get("severity").isNull() ? "" : " " + d.get("severity").asText();
            list.add(d.get("condition").asText() + severity);
        }
        return list;
    }

    private List<String> statuses(JsonNode result) {
        List<String> list = new ArrayList<>();
        for (String part : List.of("morningRoutine", "eveningRoutine", "notInRoutine")) {
            result.get(part).forEach(r -> list.add(r.get("sastojak").asText() + " " + r.get("status").asText()));
        }
        return list;
    }
}
