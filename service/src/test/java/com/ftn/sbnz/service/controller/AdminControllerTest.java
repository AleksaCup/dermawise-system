package com.ftn.sbnz.service.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ftn.sbnz.service.ApiTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AdminControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    @Test
    void knowledgeBaseOverviewListsRulesPerBaseAndPhase() throws Exception {
        String admin = ApiTestSupport.login(mvc, mapper, "admin@dermawise.rs", "admin123");

        String body = mvc.perform(get("/api/admin/knowledge-base").header("Authorization", admin))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        JsonNode info = mapper.readTree(body);

        assertThat(info.get("templateRows").asInt()).isGreaterThanOrEqualTo(5);
        JsonNode analysis = info.get("bases").get(0);
        assertThat(analysis.get("name").asText()).isEqualTo("analysis");
        assertThat(analysis.get("rules").asInt()).isGreaterThan(80);
        assertThat(analysis.get("rulesPerAgendaGroup").fieldNames()).toIterable()
                .containsExactly("diagnosis", "phototype", "safety", "therapy");
        assertThat(analysis.get("queryList").toString()).contains("imaRosaceju", "uzrocnikKontaktnogDermatitisa");
        assertThat(info.get("bases").get(2).get("ruleList").toString()).contains("Sumnja na ETR rozaceju");
    }

    @Test
    void adminCreatesDermatologistAndDeactivatesUser() throws Exception {
        String admin = ApiTestSupport.login(mvc, mapper, "admin@dermawise.rs", "admin123");

        mvc.perform(post("/api/admin/users").header("Authorization", admin).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"nova.dermatolog@dermawise.rs\",\"password\":\"derm456\","
                                + "\"fullName\":\"dr Nova\",\"role\":\"DERMATOLOGIST\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.role").value("DERMATOLOGIST"));
        String derm = ApiTestSupport.login(mvc, mapper, "nova.dermatolog@dermawise.rs", "derm456");
        mvc.perform(get("/api/dermatologist/patients").header("Authorization", derm)).andExpect(status().isOk());

        String user = ApiTestSupport.registerUser(mvc, mapper);
        String users = mvc.perform(get("/api/admin/users").header("Authorization", admin))
                .andReturn().getResponse().getContentAsString();
        JsonNode list = mapper.readTree(users);
        long lastId = list.get(list.size() - 1).get("id").asLong();

        mvc.perform(patch("/api/admin/users/" + lastId + "/active").param("active", "false")
                        .header("Authorization", admin))
                .andExpect(jsonPath("$.active").value(false));
        mvc.perform(get("/api/profile").header("Authorization", user)).andExpect(status().isUnauthorized());
    }

    @Test
    void dermatologistCannotOpenAdmin() throws Exception {
        String derm = ApiTestSupport.login(mvc, mapper, "dermatolog@dermawise.rs", "derm123");

        mvc.perform(get("/api/admin/knowledge-base").header("Authorization", derm)).andExpect(status().isForbidden());
    }
}
