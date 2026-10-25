package com.shop.domain.model;

import com.shop.domain.exception.DomainException;
import com.shop.domain.exception.InsufficientStockException;

import java.time.Instant;
import java.util.UUID;

public class Product {
    private final UUID id;
    private String name;
    private String description;
    private long priceCents;
    private String imageUrl;
    private String category;
    private int stock;
    private int salesCount;
    private boolean archived;
    private final Instant createdAt;

    public Product(UUID id, String name, String description, long priceCents, String imageUrl, String category,
                   int stock, int salesCount, boolean archived, Instant createdAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.priceCents = priceCents;
        this.imageUrl = imageUrl;
        this.category = category;
        this.stock = stock;
        this.salesCount = salesCount;
        this.archived = archived;
        this.createdAt = createdAt;
    }

    public static Product create(String name, String description, long priceCents, String imageUrl, String category, int stock) {
        validate(priceCents, stock);
        return new Product(UUID.randomUUID(), name, description, priceCents, imageUrl, category, stock, 0, false, Instant.now());
    }

    public void update(String name, String description, long priceCents, String imageUrl, String category, int stock) {
        validate(priceCents, stock);
        this.name = name;
        this.description = description;
        this.priceCents = priceCents;
        this.imageUrl = imageUrl;
        this.category = category;
        this.stock = stock;
    }

    public void updateStock(int newStock) {
        validate(priceCents, newStock);
        this.stock = newStock;
    }

    /** Reserves {@code quantity} units; fails if the stock is insufficient or the product is archived. */
    public void reserve(int quantity) {
        if (quantity <= 0) {
            throw new DomainException("Quantity must be positive");
        }
        if (archived) {
            throw new DomainException("Product '" + name + "' is no longer available");
        }
        if (stock < quantity) {
            throw new InsufficientStockException("Insufficient stock for '" + name + "' (available: " + stock + ")");
        }
        stock -= quantity;
    }

    public void release(int quantity) {
        stock += quantity;
    }

    public void recordSale(int quantity) {
        salesCount += quantity;
    }

    public void archive() {
        this.archived = true;
    }

    private static void validate(long priceCents, int stock) {
        if (priceCents < 0) throw new DomainException("Price must not be negative");
        if (stock < 0) throw new DomainException("Stock must not be negative");
    }

    public UUID getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public long getPriceCents() { return priceCents; }
    public String getImageUrl() { return imageUrl; }
    public String getCategory() { return category; }
    public int getStock() { return stock; }
    public int getSalesCount() { return salesCount; }
    public boolean isArchived() { return archived; }
    public Instant getCreatedAt() { return createdAt; }
}
