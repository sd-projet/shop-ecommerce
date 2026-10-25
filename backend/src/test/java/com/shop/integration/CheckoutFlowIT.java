package com.shop.integration;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** End-to-end purchase journey with the fake payment gateway (default provider). */
class CheckoutFlowIT extends AbstractIntegrationTest {

    private static final Map<String, String> ADDRESS =
            Map.of("fullName", "Jane Doe", "street", "1 rue de Paris", "city", "Paris", "postalCode", "75001", "country", "France");

    private JsonNode firstProduct(String query) throws Exception {
        String body = mvc.perform(get("/api/products").param("q", query)).andReturn().getResponse().getContentAsString();
        return read(body).get("content").get(0);
    }

    private Map<String, Object> checkoutBody(JsonNode product, int quantity) {
        return Map.of("items", List.of(Map.of("productId", product.get("id").asText(), "quantity", quantity)),
                "address", ADDRESS, "shippingMethod", "STANDARD");
    }

    @Test
    void customerPlacesPaysAndAdminShipsAnOrder() throws Exception {
        String customer = registerCustomer();
        JsonNode product = firstProduct("Câble USB-C");
        int stockBefore = product.get("stock").asInt();

        String created = mvc.perform(jsonPost("/api/orders", checkoutBody(product, 2)).header("Authorization", bearer(customer)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.order.status").value("PENDING"))
                .andExpect(jsonPath("$.order.totalCents").value(2 * 1200 + 490))
                .andExpect(jsonPath("$.clientSecret").isNotEmpty())
                .andReturn().getResponse().getContentAsString();
        String orderId = read(created).at("/order/id").asText();

        // stock reserved
        JsonNode after = read(mvc.perform(get("/api/products/" + product.get("id").asText())).andReturn().getResponse().getContentAsString());
        assertThat(after.get("stock").asInt()).isEqualTo(stockBefore - 2);

        mvc.perform(post("/api/orders/" + orderId + "/confirm-payment").header("Authorization", bearer(customer)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PAID"));

        mvc.perform(get("/api/orders").header("Authorization", bearer(customer)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1));

        String admin = loginAdmin();
        mvc.perform(patch("/api/admin/orders/" + orderId + "/status").header("Authorization", bearer(admin))
                        .contentType("application/json").content("{\"status\":\"SHIPPED\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SHIPPED"));

        // a customer cannot update statuses
        mvc.perform(patch("/api/admin/orders/" + orderId + "/status").header("Authorization", bearer(customer))
                        .contentType("application/json").content("{\"status\":\"DELIVERED\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void cannotOrderMoreThanAvailableStock() throws Exception {
        String customer = registerCustomer();
        JsonNode product = firstProduct("Webcam");
        mvc.perform(jsonPost("/api/orders", checkoutBody(product, 50)).header("Authorization", bearer(customer)))
                .andExpect(status().isConflict());
    }

    @Test
    void cancellingAPendingOrderRestoresStock() throws Exception {
        String customer = registerCustomer();
        JsonNode product = firstProduct("Support ordinateur");
        int stockBefore = product.get("stock").asInt();

        String created = mvc.perform(jsonPost("/api/orders", checkoutBody(product, 3)).header("Authorization", bearer(customer)))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        String orderId = read(created).at("/order/id").asText();

        mvc.perform(post("/api/orders/" + orderId + "/cancel").header("Authorization", bearer(customer)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("CANCELLED"));

        JsonNode after = read(mvc.perform(get("/api/products/" + product.get("id").asText())).andReturn().getResponse().getContentAsString());
        assertThat(after.get("stock").asInt()).isEqualTo(stockBefore);
    }

    @Test
    void customersCannotReadEachOthersOrders() throws Exception {
        String owner = registerCustomer();
        String intruder = registerCustomer();
        JsonNode product = firstProduct("Câble USB-C");
        String created = mvc.perform(jsonPost("/api/orders", checkoutBody(product, 1)).header("Authorization", bearer(owner)))
                .andReturn().getResponse().getContentAsString();
        String orderId = read(created).at("/order/id").asText();

        mvc.perform(get("/api/orders/" + orderId).header("Authorization", bearer(intruder))).andExpect(status().isForbidden());
        mvc.perform(get("/api/orders/" + orderId).header("Authorization", bearer(owner))).andExpect(status().isOk());
    }
}
