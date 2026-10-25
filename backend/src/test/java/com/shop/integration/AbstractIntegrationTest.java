package com.shop.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.testcontainers.containers.PostgreSQLContainer;

import java.util.Map;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/** Boots the full application against a real PostgreSQL started by Testcontainers (requires Docker). */
@SpringBootTest
@AutoConfigureMockMvc
public abstract class AbstractIntegrationTest {

    @ServiceConnection
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

    static {
        POSTGRES.start();
    }

    @Autowired protected MockMvc mvc;
    @Autowired protected ObjectMapper mapper;

    protected String json(Object value) throws Exception {
        return mapper.writeValueAsString(value);
    }

    protected MockHttpServletRequestBuilder jsonPost(String url, Object body) throws Exception {
        return post(url).contentType(MediaType.APPLICATION_JSON).content(json(body));
    }

    /** Registers a fresh customer and returns its JWT. */
    protected String registerCustomer() throws Exception {
        String email = "user-" + UUID.randomUUID() + "@example.com";
        String body = mvc.perform(jsonPost("/api/auth/register",
                        Map.of("email", email, "password", "password123", "firstName", "Jane", "lastName", "Doe")))
                .andReturn().getResponse().getContentAsString();
        return mapper.readTree(body).get("token").asText();
    }

    protected String loginAdmin() throws Exception {
        String body = mvc.perform(jsonPost("/api/auth/login", Map.of("email", "admin@shop.com", "password", "Admin123!")))
                .andReturn().getResponse().getContentAsString();
        return mapper.readTree(body).get("token").asText();
    }

    protected JsonNode read(String body) throws Exception {
        return mapper.readTree(body);
    }

    protected static String bearer(String token) {
        return "Bearer " + token;
    }
}
