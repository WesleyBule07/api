package com.example.demo;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

class AuthIntegrationTest extends IntegrationTestBase {

    @Autowired
    private ObjectMapper objectMapper;

    private static final String REGISTER = """
            {"username":"neto.mabjaia","email":"neto@example.com","password":"password123",
             "fullName":"Neto Mabjaia","province":"Maputo"}
            """;

    private static final String LOGIN = """
            {"username":"neto.mabjaia","password":"password123"}
            """;

    @Test
    void registerReturnsTokensAndUser() throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType("application/json")
                        .content(REGISTER))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty())
                .andExpect(jsonPath("$.user.username").value("neto.mabjaia"))
                .andExpect(jsonPath("$.user.role").value("USER"));
    }

    @Test
    void duplicateRegisterReturnsConflict() throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType("application/json")
                        .content(REGISTER))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType("application/json")
                        .content(REGISTER))
                .andExpect(status().isConflict())
                .andExpect(content().contentType("application/problem+json"))
                .andExpect(jsonPath("$.type").value("urn:problem-type:email-already-in-use"));
    }

    @Test
    void loginReturnsTokens() throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType("application/json")
                        .content(REGISTER))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType("application/json")
                        .content(LOGIN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.accessToken").isNotEmpty());
    }

    @Test
    void loginWithBadPasswordReturnsUnauthorized() throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType("application/json")
                        .content(REGISTER))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType("application/json")
                        .content("""
                                {"username":"neto.mabjaia","password":"wrongpassword"}
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.type").value("urn:problem-type:invalid-credentials"));
    }

    @Test
    void refreshReturnsNewAccessToken() throws Exception {
        String response = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType("application/json")
                        .content(REGISTER))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        JsonNode node = objectMapper.readTree(response);
        String refreshToken = node.get("refreshToken").asText();

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType("application/json")
                        .content("{\"refreshToken\":\"" + refreshToken + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty());
    }

    @Test
    void validationErrorsReturnBadRequest() throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType("application/json")
                        .content("""
                                {"username":"ab","email":"nope","password":"x"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.type").value("urn:problem-type:validation-error"))
                .andExpect(jsonPath("$.fieldErrors", hasSize(3)));
    }
}