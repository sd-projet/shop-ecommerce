package com.shop.domain.model;

public record ProductSearchCriteria(
        String query,
        String category,
        Long minPriceCents,
        Long maxPriceCents,
        boolean inStockOnly,
        boolean includeArchived,
        ProductSort sort,
        int page,
        int size) {
}
