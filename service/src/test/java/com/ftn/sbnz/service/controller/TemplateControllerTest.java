package com.ftn.sbnz.service.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ftn.sbnz.service.ApiTestSupport;
import com.ftn.sbnz.service.config.KieBaseHolder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class TemplateControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private KieBaseHolder kieBaseHolder;

    private double adapalene(String userToken) throws Exception {
        JsonNode analysis = ApiTestSupport.analyse(mvc, mapper, userToken);
        for (JsonNode r : analysis.get("eveningRoutine")) {
            if (r.get("sastojak").asText().equals("adapalen")) {
                return r.get("koncentracija").asDouble();
            }
        }
        throw new AssertionError("adapalen not in routine");
    }

    @Test
    void newRowChangesTheNextAnalysisWithoutRestart() throws Exception {
        String derm = ApiTestSupport.login(mvc, mapper, "dermatolog@dermawise.rs", "derm123");
        String user = ApiTestSupport.registerUser(mvc, mapper);
        ApiTestSupport.submit(mvc, user, ApiTestSupport.questionnaire(mapper, "moderate-acne"));
        assertThat(adapalene(user)).isEqualTo(0.1);
        long versionBefore = kieBaseHolder.getVersion();

        String created = mvc.perform(post("/api/templates").header("Authorization", derm)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sastojak\":\"Adapalen\",\"minFototip\":1,\"maxFototip\":3,"
                                + "\"maxKoncentracija\":0.05,\"napomena\":\"niža koncentracija na početku terapije\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.sastojak").value("adapalen"))
                .andReturn().getResponse().getContentAsString();
        long id = mapper.readTree(created).get("id").asLong();

        assertThat(kieBaseHolder.getVersion()).isEqualTo(versionBefore + 1);
        assertThat(adapalene(user)).isEqualTo(0.05);

        mvc.perform(get("/api/templates/generated-drl").header("Authorization", derm))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("sastojak == \"adapalen\"")));

        mvc.perform(delete("/api/templates/" + id).header("Authorization", derm)).andExpect(status().isNoContent());
        assertThat(adapalene(user)).isEqualTo(0.1);
    }

    @Test
    void unsafeRowIsRejectedAndRulesStay() throws Exception {
        String derm = ApiTestSupport.login(mvc, mapper, "dermatolog@dermawise.rs", "derm123");
        long versionBefore = kieBaseHolder.getVersion();

        mvc.perform(post("/api/templates").header("Authorization", derm).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sastojak\":\"retinol\",\"minFototip\":1,\"maxFototip\":6,"
                                + "\"maxKoncentracija\":1,\"napomena\":\"\\\"); retract($u); //\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Napomena ne sme da sadrži znake \" \\ @ $"));

        mvc.perform(post("/api/templates").header("Authorization", derm).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sastojak\":\"retinol\",\"minFototip\":5,\"maxFototip\":2,"
                                + "\"maxKoncentracija\":1,\"napomena\":\"x\"}"))
                .andExpect(status().isBadRequest());

        assertThat(kieBaseHolder.getVersion()).isEqualTo(versionBefore);
        mvc.perform(get("/api/templates").header("Authorization", derm))
                .andExpect(jsonPath("$.length()").value(5));
    }

    @Test
    void usersCannotEditTemplates() throws Exception {
        String user = ApiTestSupport.login(mvc, mapper, "ana@dermawise.rs", "user123");

        mvc.perform(get("/api/templates").header("Authorization", user)).andExpect(status().isForbidden());
    }
}
