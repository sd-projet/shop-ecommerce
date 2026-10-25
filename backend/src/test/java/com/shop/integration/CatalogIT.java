package com.shop.integration;

import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Relies on the demo catalogue inserted by Flyway migration V2. */
class CatalogIT extends AbstractIntegrationTest {

    @Test
    void listsProductsWithPagination() throws Exception {
        mvc.perform(get("/api/products").param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(5))
                .andExpect(jsonPath("$.totalElements").value(16));
    }

    @Test
    void filtersByCategory() throws Exception {
        mvc.perform(get("/api/products").param("category", "Audio"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(3));
    }

    @Test
    void sortsByPriceAscending() throws Exception {
        mvc.perform(get("/api/products").param("sort", "PRICE_ASC"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].priceCents").value(1200));
    }

    @Test
    void searchesAcrossNameAndDescription() throws Exception {
        mvc.perform(get("/api/products").param("q", "clavier mécanique"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].name").value("Clavier mécanique Forge"));
    }

    @Test
    void inStockFilterHidesSoldOutProducts() throws Exception {
        mvc.perform(get("/api/products").param("q", "batterie").param("inStock", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    void exposesCategories() throws Exception {
        mvc.perform(get("/api/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(5));
    }
}
