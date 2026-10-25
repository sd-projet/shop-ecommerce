package com.shop.domain.model;

import java.util.UUID;

/** Snapshot of a product at the time of purchase. */
public record OrderItem(UUID productId, String productName, long unitPriceCents, int quantity) {
    public long totalCents() {
        return unitPriceCents * quantity;
    }
}
