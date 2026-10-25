package com.shop.domain.model;

import java.util.Map;

public record SalesStats(
        long revenueCents,
        long totalOrders,
        Map<OrderStatus, Long> ordersByStatus,
        long activeProducts,
        long lowStockProducts,
        long customers) {
}
