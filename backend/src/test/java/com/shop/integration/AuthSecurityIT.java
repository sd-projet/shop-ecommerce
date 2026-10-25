package com.shop.integration;

import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthSecurityIT extends AbstractIntegrationTest {

    @Test
    void registerThenLoginReturnsJwt() throws Exception {
        String email = "john-" + UUID.randomUUID() + "@example.com";
        var payload = Map.of("email", email, "password", "password123", "firstName", "John", "lastName", "Doe");

        mvc.perform(jsonPost("/api/auth/register", payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.user.role").value("ROLE_CUSTOMER"));

        mvc.perform(jsonPost("/api/auth/register", payload)).andExpect(status().isConflict());

        mvc.perform(jsonPost("/api/auth/login", Map.of("email", email, "password", "password123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty());

        mvc.perform(jsonPost("/api/auth/login", Map.of("email", email, "password", "wrong-password")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void invalidRegistrationPayloadIsRejected() throws Exception {
        mvc.perform(jsonPost("/api/auth/register", Map.of("email", "not-an-email", "password", "short", "firstName", "A", "lastName", "B")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.email").exists());
    }

    @Test
    void protectedEndpointsRequireAuthentication() throws Exception {
        mvc.perform(get("/api/orders")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/users/me")).andExpect(status().isUnauthorized());
    }

    @Test
    void adminEndpointsAreReservedToAdmins() throws Exception {
        String customer = registerCustomer();
        mvc.perform(get("/api/admin/dashboard").header("Authorization", bearer(customer))).andExpect(status().isForbidden());
        mvc.perform(get("/api/admin/dashboard")).andExpect(status().isUnauthorized());

        mvc.perform(get("/api/admin/dashboard").header("Authorization", bearer(loginAdmin())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activeProducts").isNumber());
    }
}
