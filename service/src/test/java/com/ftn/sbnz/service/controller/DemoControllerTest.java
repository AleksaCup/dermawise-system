package com.ftn.sbnz.service.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ftn.sbnz.service.ApiTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
class DemoControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    @Test
    void demoWeekTriggersBothCepExamples() throws Exception {
        String token = ApiTestSupport.registerUser(mvc, mapper);
        ApiTestSupport.submit(mvc, token, ApiTestSupport.questionnaire(mapper, "moderate-acne"));
        ApiTestSupport.analyse(mvc, mapper, token);

        String body = mvc.perform(post("/api/demo/cep-week").header("Authorization", token))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        JsonNode result = mapper.readTree(body);

        List<String> types = new ArrayList<>();
        result.get("alerts").forEach(a -> types.add(a.get("type").asText()));
        assertThat(types).contains("ROSACEA_SUSPICION", "DOMINANT_TRIGGER", "PHOTOAGING_RISK");
        assertThat(types).filteredOn("HIGH_UV_EXPOSURE"::equals).hasSize(5);
        List<String> recommended = new ArrayList<>();
        result.get("recommendations").forEach(r -> recommended.add(r.get("sastojak").asText()));
        assertThat(recommended).containsExactlyInAnyOrder("vitamin c", "tretinoin");
    }
}
