package com.shop.domain.repository;

import com.shop.domain.model.*;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public interface OrderRepository {
    Order save(Order order);
    Optional<Order> findById(UUID id);
    Optional<Order> findByPaymentIntentId(String paymentIntentId);
    PageResult<Order> findByUserId(UUID userId, int page, int size);
    PageResult<Order> findAll(OrderStatus statusFilter, int page, int size);
    long sumRevenueCents();
    Map<OrderStatus, Long> countByStatus();
}
