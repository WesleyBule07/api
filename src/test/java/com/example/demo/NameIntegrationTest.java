package com.example.demo;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class NameIntegrationTest extends IntegrationTestBase {

    @Autowired
    private ObjectMapper objectMapper;

    private String token;

    @BeforeEach
    void registerClientForFavorites() throws Exception {
        String response = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType("application/json")
                        .content("""
                                {"username":"somana.tembe","email":"somana@example.com","password":"password123"}
                                """))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        JsonNode node = objectMapper.readTree(response);
        token = node.get("accessToken").asText();
    }

    @Test
    void searchAcceptsQueryAndOriginFilters() throws Exception {
        mockMvc.perform(get("/api/v1/names")
                        .param("query", "ndl")
                        .param("origin", "Tsonga"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name").value("Ndlovu"));
    }

    @Test
    void suggestReturnsLimitedRandomNames() throws Exception {
        mockMvc.perform(get("/api/v1/names/suggest")
                        .param("gender", "FEMALE")
                        .param("count", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].gender").value("FEMALE"));
    }

    @Test
    void nameNotFoundReturns404() throws Exception {
        mockMvc.perform(get("/api/v1/names/999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.type").value("urn:problem-type:name-not-found"));
    }

    @Test
    void originsAndProvincesAreListed() throws Exception {
        mockMvc.perform(get("/api/v1/names/origins"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isNotEmpty());

        mockMvc.perform(get("/api/v1/names/provinces"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isNotEmpty());
    }

    @Test
    void favoriteRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/api/v1/names/favorites"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void favoriteAndUnfavoriteLifecycle() throws Exception {
        String search = mockMvc.perform(get("/api/v1/names")
                        .param("query", "Ndlovu"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        long id = objectMapper.readTree(search).get(0).get("id").asLong();

        mockMvc.perform(post("/api/v1/names/" + id + "/favorite")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Ndlovu"));

        mockMvc.perform(get("/api/v1/names/favorites")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        mockMvc.perform(delete("/api/v1/names/" + id + "/favorite")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/names/favorites")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }
}