package com.shop.infrastructure.persistence.adapter;

import com.shop.domain.model.*;
import com.shop.domain.repository.OrderRepository;
import com.shop.infrastructure.persistence.entity.OrderEntity;
import com.shop.infrastructure.persistence.entity.OrderItemEntity;
import com.shop.infrastructure.persistence.jpa.OrderJpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class OrderRepositoryAdapter implements OrderRepository {

    private final OrderJpaRepository jpa;

    public OrderRepositoryAdapter(OrderJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Order save(Order o) {
        OrderEntity entity = jpa.findById(o.getId()).orElseGet(() -> newEntity(o));
        entity.updateState(o.getStatus(), o.getPaymentIntentId(), o.getUpdatedAt());
        return toDomain(jpa.save(entity));
    }

    @Override
    public Optional<Order> findById(UUID id) {
        return jpa.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<Order> findByPaymentIntentId(String paymentIntentId) {
        return jpa.findByPaymentIntentId(paymentIntentId).map(this::toDomain);
    }

    @Override
    public PageResult<Order> findByUserId(UUID userId, int page, int size) {
        return toPage(jpa.findByUserId(userId, pageable(page, size)));
    }

    @Override
    public PageResult<Order> findAll(OrderStatus status, int page, int size) {
        Pageable pageable = pageable(page, size);
        return toPage(status == null ? jpa.findAll(pageable) : jpa.findByStatus(status, pageable));
    }

    @Override
    public long sumRevenueCents() {
        List<OrderStatus> revenue = Arrays.stream(OrderStatus.values()).filter(OrderStatus::isRevenue).toList();
        return jpa.sumRevenue(revenue);
    }

    @Override
    public Map<OrderStatus, Long> countByStatus() {
        Map<OrderStatus, Long> result = new EnumMap<>(OrderStatus.class);
        for (Object[] row : jpa.countGroupedByStatus()) {
            result.put((OrderStatus) row[0], (Long) row[1]);
        }
        return result;
    }

    private Pageable pageable(int page, int size) {
        return PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100), Sort.by(Sort.Direction.DESC, "createdAt"));
    }

    private PageResult<Order> toPage(Page<OrderEntity> page) {
        return new PageResult<>(page.getContent().stream().map(this::toDomain).toList(),
                page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages());
    }

    private OrderEntity newEntity(Order o) {
        Address a = o.getShippingAddress();
        OrderEntity entity = new OrderEntity(o.getId(), o.getUserId(), o.getStatus(), o.getShippingMethod(),
                o.itemsTotalCents(), o.shippingCents(), o.getPaymentIntentId(), a.fullName(), a.street(), a.city(),
                a.postalCode(), a.country(), o.getCreatedAt(), o.getUpdatedAt());
        for (OrderItem i : o.getItems()) {
            entity.getItems().add(new OrderItemEntity(UUID.randomUUID(), entity, i.productId(), i.productName(), i.unitPriceCents(), i.quantity()));
        }
        return entity;
    }

    private Order toDomain(OrderEntity e) {
        List<OrderItem> items = e.getItems().stream()
                .map(i -> new OrderItem(i.getProductId(), i.getProductName(), i.getUnitPriceCents(), i.getQuantity()))
                .toList();
        Address address = new Address(e.getShipFullName(), e.getShipStreet(), e.getShipCity(), e.getShipPostalCode(), e.getShipCountry());
        return new Order(e.getId(), e.getUserId(), items, e.getStatus(), address, e.getShippingMethod(),
                e.getPaymentIntentId(), e.getCreatedAt(), e.getUpdatedAt());
    }
}
