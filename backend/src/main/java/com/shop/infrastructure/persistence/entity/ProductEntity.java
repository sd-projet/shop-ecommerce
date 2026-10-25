package com.shop.infrastructure.persistence.entity;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "products")
public class ProductEntity {
    @Id
    private UUID id;
    @Column(nullable = false)
    private String name;
    @Column(columnDefinition = "text")
    private String description;
    @Column(name = "price_cents", nullable = false)
    private long priceCents;
    @Column(name = "image_url")
    private String imageUrl;
    private String category;
    @Column(nullable = false)
    private int stock;
    @Column(name = "sales_count", nullable = false)
    private int salesCount;
    @Column(nullable = false)
    private boolean archived;
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected ProductEntity() {}

    public ProductEntity(UUID id, String name, String description, long priceCents, String imageUrl, String category,
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
