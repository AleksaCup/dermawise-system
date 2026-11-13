package com.ftn.sbnz.service.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void registerThenLoginGivesUserToken() throws Exception {
        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"Novi@Test.rs\",\"password\":\"lozinka1\",\"fullName\":\"Novi Korisnik\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.role").value("USER"))
                .andExpect(jsonPath("$.email").value("novi@test.rs"));

        String body = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"novi@test.rs\",\"password\":\"lozinka1\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        JsonNode json = objectMapper.readTree(body);

        mvc.perform(get("/api/admin/users").header("Authorization", "Bearer " + json.get("token").asText()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    void wrongPasswordIsRejected() throws Exception {
        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"druga@test.rs\",\"password\":\"lozinka1\",\"fullName\":\"Druga\"}"));

        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"druga@test.rs\",\"password\":\"pogresna\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Pogrešan email ili lozinka"));
    }

    @Test
    void invalidRegistrationListsFieldErrors() throws Exception {
        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"nije-email\",\"password\":\"123\",\"fullName\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.email").exists())
                .andExpect(jsonPath("$.fieldErrors.password").value("Lozinka mora imati bar 6 znakova"))
                .andExpect(jsonPath("$.fieldErrors.fullName").exists());
    }

    @Test
    void protectedEndpointWithoutTokenIs401() throws Exception {
        mvc.perform(get("/api/analysis/latest"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Potrebna je prijava"));
    }

    @Test
    void forgedTokenIs401() throws Exception {
        mvc.perform(get("/api/analysis/latest").header("Authorization", "Bearer abc.def.ghi"))
                .andExpect(status().isUnauthorized());
    }
}
