package com.shop.infrastructure.persistence.entity;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "order_items")
public class OrderItemEntity {
    @Id
    private UUID id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id")
    private OrderEntity order;
    @Column(name = "product_id", nullable = false)
    private UUID productId;
    @Column(name = "product_name", nullable = false)
    private String productName;
    @Column(name = "unit_price_cents", nullable = false)
    private long unitPriceCents;
    @Column(nullable = false)
    private int quantity;

    protected OrderItemEntity() {}

    public OrderItemEntity(UUID id, OrderEntity order, UUID productId, String productName, long unitPriceCents, int quantity) {
        this.id = id;
        this.order = order;
        this.productId = productId;
        this.productName = productName;
        this.unitPriceCents = unitPriceCents;
        this.quantity = quantity;
    }

    public UUID getProductId() { return productId; }
    public String getProductName() { return productName; }
    public long getUnitPriceCents() { return unitPriceCents; }
    public int getQuantity() { return quantity; }
}
