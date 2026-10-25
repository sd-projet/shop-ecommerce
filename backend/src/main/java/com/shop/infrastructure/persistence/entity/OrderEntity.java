package com.shop.infrastructure.persistence.entity;

import com.shop.domain.model.OrderStatus;
import com.shop.domain.model.ShippingMethod;
import jakarta.persistence.*;
import org.hibernate.annotations.BatchSize;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "orders")
public class OrderEntity {
    @Id
    private UUID id;
    @Column(name = "user_id", nullable = false)
    private UUID userId;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;
    @Enumerated(EnumType.STRING)
    @Column(name = "shipping_method", nullable = false)
    private ShippingMethod shippingMethod;
    @Column(name = "items_total_cents", nullable = false)
    private long itemsTotalCents;
    @Column(name = "shipping_cents", nullable = false)
    private long shippingCents;
    @Column(name = "payment_intent_id")
    private String paymentIntentId;
    @Column(name = "ship_full_name")
    private String shipFullName;
    @Column(name = "ship_street")
    private String shipStreet;
    @Column(name = "ship_city")
    private String shipCity;
    @Column(name = "ship_postal_code")
    private String shipPostalCode;
    @Column(name = "ship_country")
    private String shipCountry;
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @BatchSize(size = 50)
    private List<OrderItemEntity> items = new ArrayList<>();

    protected OrderEntity() {}

    public OrderEntity(UUID id, UUID userId, OrderStatus status, ShippingMethod shippingMethod, long itemsTotalCents,
                       long shippingCents, String paymentIntentId, String shipFullName, String shipStreet, String shipCity,
                       String shipPostalCode, String shipCountry, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.userId = userId;
        this.status = status;
        this.shippingMethod = shippingMethod;
        this.itemsTotalCents = itemsTotalCents;
        this.shippingCents = shippingCents;
        this.paymentIntentId = paymentIntentId;
        this.shipFullName = shipFullName;
        this.shipStreet = shipStreet;
        this.shipCity = shipCity;
        this.shipPostalCode = shipPostalCode;
        this.shipCountry = shipCountry;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /** Only these fields change after creation; items are immutable snapshots. */
    public void updateState(OrderStatus status, String paymentIntentId, Instant updatedAt) {
        this.status = status;
        this.paymentIntentId = paymentIntentId;
        this.updatedAt = updatedAt;
    }

    public UUID getId() { return id; }
    public UUID getUserId() { return userId; }
    public OrderStatus getStatus() { return status; }
    public ShippingMethod getShippingMethod() { return shippingMethod; }
    public String getPaymentIntentId() { return paymentIntentId; }
    public String getShipFullName() { return shipFullName; }
    public String getShipStreet() { return shipStreet; }
    public String getShipCity() { return shipCity; }
    public String getShipPostalCode() { return shipPostalCode; }
    public String getShipCountry() { return shipCountry; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public List<OrderItemEntity> getItems() { return items; }
}
