package com.ftn.sbnz.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

public final class ApiTestSupport {

    private ApiTestSupport() {
    }

    public static String registerUser(MockMvc mvc, ObjectMapper mapper) throws Exception {
        String email = "korisnik-" + UUID.randomUUID() + "@test.rs";
        String body = mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"lozinka1\",\"fullName\":\"Test Korisnik\"}"))
                .andReturn().getResponse().getContentAsString();
        return "Bearer " + mapper.readTree(body).get("token").asText();
    }

    public static String login(MockMvc mvc, ObjectMapper mapper, String email, String password) throws Exception {
        String body = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}"))
                .andReturn().getResponse().getContentAsString();
        return "Bearer " + mapper.readTree(body).get("token").asText();
    }

    public static ObjectNode questionnaire(ObjectMapper mapper, String name) throws IOException {
        try (InputStream in = new ClassPathResource("questionnaires/" + name + ".json").getInputStream()) {
            return (ObjectNode) mapper.readTree(in);
        }
    }

    public static ResultActions submit(MockMvc mvc, String token, JsonNode questionnaire) throws Exception {
        return mvc.perform(put("/api/profile/questionnaire").header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON).content(questionnaire.toString()));
    }

    public static JsonNode analyse(MockMvc mvc, ObjectMapper mapper, String token) throws Exception {
        String body = mvc.perform(post("/api/analysis").header("Authorization", token))
                .andReturn().getResponse().getContentAsString();
        return mapper.readTree(body);
    }
}
