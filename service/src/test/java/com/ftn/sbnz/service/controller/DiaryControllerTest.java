package com.ftn.sbnz.service.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.ftn.sbnz.service.ApiTestSupport;
import com.ftn.sbnz.service.service.CepSessionManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class DiaryControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private CepSessionManager sessionManager;

    private JsonNode postJson(String token, String path, String json) throws Exception {
        String body = mvc.perform(post(path)
                        .header("Authorization", token).contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return mapper.readTree(body);
    }

    private JsonNode flushing(String token, Instant at, String trigger) throws Exception {
        return postJson(token, "/api/diary/flushing",
                "{\"occurredAt\":\"" + at + "\",\"durationMinutes\":20,\"trigger\":\"" + trigger + "\"}");
    }

    private static List<String> alertTypes(JsonNode response) {
        List<String> types = new ArrayList<>();
        response.get("alerts").forEach(a -> types.add(a.get("type").asText()));
        return types;
    }

    @Test
    void fourthFlushingEpisodeRaisesSuspicionOnlyOnceEvenAfterReplay() throws Exception {
        String token = ApiTestSupport.registerUser(mvc, mapper);
        Instant now = Instant.now();

        assertThat(alertTypes(flushing(token, now.minus(Duration.ofDays(5)), "ALKOHOL"))).isEmpty();
        assertThat(alertTypes(flushing(token, now.minus(Duration.ofDays(4)), "TOPLOTA"))).isEmpty();
        assertThat(alertTypes(flushing(token, now.minus(Duration.ofDays(2)), "ALKOHOL"))).isEmpty();
        JsonNode fourth = flushing(token, now.minus(Duration.ofDays(1)), "ALKOHOL");
        assertThat(alertTypes(fourth)).containsExactlyInAnyOrder("ROSACEA_SUSPICION", "DOMINANT_TRIGGER");

        int openBefore = sessionManager.openSessions();
        ApiTestSupport.submit(mvc, token, ApiTestSupport.questionnaire(mapper, "rosacea-ppr"));
        assertThat(sessionManager.openSessions()).isEqualTo(openBefore - 1);

        JsonNode fifth = flushing(token, now.minus(Duration.ofHours(2)), "HRANA");
        assertThat(alertTypes(fifth)).isEmpty();

        mvc.perform(get("/api/cep/alerts").header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
        mvc.perform(get("/api/diary").header("Authorization", token))
                .andExpect(jsonPath("$.length()").value(5));
    }

    @Test
    void newProductReactionGivesAlertAndBackwardFindsTheCause() throws Exception {
        String token = ApiTestSupport.registerUser(mvc, mapper);
        ObjectNode questionnaire = ApiTestSupport.questionnaire(mapper, "moderate-acne");
        ArrayNode allergies = ((ObjectNode) questionnaire.get("lifestyle")).putArray("allergies");
        allergies.add("Methylisothiazolinone");
        ApiTestSupport.submit(mvc, token, questionnaire);
        Instant now = Instant.now();

        postJson(token, "/api/diary/products", "{\"occurredAt\":\"" + now.minus(Duration.ofDays(4)) + "\","
                + "\"product\":{\"name\":\"Losion za lice\",\"ingredients\":[{\"name\":\"Aqua\"},{\"name\":\"Kathon CG\"}],"
                + "\"applicationAreas\":[\"CHEEKS\",\"FOREHEAD\"]}}");
        JsonNode symptom = postJson(token, "/api/diary/symptoms", "{\"occurredAt\":\"" + now.minus(Duration.ofDays(1))
                + "\",\"type\":\"ERYTHEMA\",\"location\":\"CHEEKS\"}");
        assertThat(alertTypes(symptom)).containsExactly("CONTACT_REACTION");

        mvc.perform(get("/api/backward/contact-dermatitis-cause").header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.sastojak == 'kathon cg')].proizvod").value("Losion za lice"));

        JsonNode analysis = ApiTestSupport.analyse(mvc, mapper, token);
        List<String> conditions = new ArrayList<>();
        analysis.get("diagnoses").forEach(d -> conditions.add(d.get("condition").asText()));
        assertThat(conditions).contains("CONTACT_DERMATITIS", "ACNE");
        assertThat(analysis.get("contactDermatitisCauses").toString()).contains("methylisothiazolinone");
    }

    @Test
    void futureEntryIs400() throws Exception {
        String token = ApiTestSupport.registerUser(mvc, mapper);

        mvc.perform(post("/api/diary/flushing").header("Authorization", token).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"occurredAt\":\"" + Instant.now().plus(Duration.ofDays(2)) + "\",\"durationMinutes\":10}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Unos u dnevnik ne može biti u budućnosti"));
    }
}
