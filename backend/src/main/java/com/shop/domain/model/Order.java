package com.shop.domain.model;

import com.shop.domain.exception.DomainException;
import com.shop.domain.exception.InvalidOrderStateException;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class Order {
    private final UUID id;
    private final UUID userId;
    private final List<OrderItem> items;
    private OrderStatus status;
    private final Address shippingAddress;
    private final ShippingMethod shippingMethod;
    private String paymentIntentId;
    private final Instant createdAt;
    private Instant updatedAt;

    public Order(UUID id, UUID userId, List<OrderItem> items, OrderStatus status, Address shippingAddress,
                 ShippingMethod shippingMethod, String paymentIntentId, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.userId = userId;
        this.items = List.copyOf(items);
        this.status = status;
        this.shippingAddress = shippingAddress;
        this.shippingMethod = shippingMethod;
        this.paymentIntentId = paymentIntentId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Order place(UUID userId, List<OrderItem> items, Address address, ShippingMethod method) {
        if (items == null || items.isEmpty()) {
            throw new DomainException("An order must contain at least one item");
        }
        Instant now = Instant.now();
        return new Order(UUID.randomUUID(), userId, items, OrderStatus.PENDING, address, method, null, now, now);
    }

    public long itemsTotalCents() {
        return items.stream().mapToLong(OrderItem::totalCents).sum();
    }

    public long shippingCents() {
        return shippingMethod.costCents();
    }

    public long totalCents() {
        return itemsTotalCents() + shippingCents();
    }

    public void attachPaymentIntent(String paymentIntentId) {
        this.paymentIntentId = paymentIntentId;
        this.updatedAt = Instant.now();
    }

    public void changeStatus(OrderStatus target) {
        if (!status.canTransitionTo(target)) {
            throw new InvalidOrderStateException("Cannot move an order from " + status + " to " + target);
        }
        this.status = target;
        this.updatedAt = Instant.now();
    }

    public boolean isPaid() {
        return status != OrderStatus.PENDING && status != OrderStatus.CANCELLED;
    }

    public boolean belongsTo(UUID candidate) {
        return userId.equals(candidate);
    }

    public UUID getId() { return id; }
    public UUID getUserId() { return userId; }
    public List<OrderItem> getItems() { return items; }
    public OrderStatus getStatus() { return status; }
    public Address getShippingAddress() { return shippingAddress; }
    public ShippingMethod getShippingMethod() { return shippingMethod; }
    public String getPaymentIntentId() { return paymentIntentId; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
