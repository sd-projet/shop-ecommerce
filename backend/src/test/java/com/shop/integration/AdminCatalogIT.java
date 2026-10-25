package com.shop.integration;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AdminCatalogIT extends AbstractIntegrationTest {

    @Test
    void adminCanCreateUpdateRestockAndArchiveAProduct() throws Exception {
        String admin = bearer(loginAdmin());
        var body = Map.of("name", "Produit test", "description", "desc", "priceCents", 1990,
                "imageUrl", "https://example.com/i.png", "category", "Test", "stock", 10);

        String created = mvc.perform(jsonPost("/api/admin/products", body).header("Authorization", admin))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Produit test"))
                .andReturn().getResponse().getContentAsString();
        String id = read(created).get("id").asText();

        mvc.perform(patch("/api/admin/products/" + id + "/stock").header("Authorization", admin)
                        .contentType("application/json").content("{\"stock\":42}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.stock").value(42));

        mvc.perform(delete("/api/admin/products/" + id).header("Authorization", admin)).andExpect(status().isNoContent());

        // archived products disappear from the public catalogue
        mvc.perform(get("/api/products/" + id)).andExpect(status().isNotFound());
    }

    @Test
    void customersCannotManageProducts() throws Exception {
        String customer = bearer(registerCustomer());
        var body = Map.of("name", "X", "description", "d", "priceCents", 100, "category", "Test", "stock", 1);
        mvc.perform(jsonPost("/api/admin/products", body).header("Authorization", customer)).andExpect(status().isForbidden());
    }
}
