package com.shop.infrastructure.persistence.jpa;

import com.shop.domain.model.OrderStatus;
import com.shop.infrastructure.persistence.entity.OrderEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrderJpaRepository extends JpaRepository<OrderEntity, UUID> {
    Optional<OrderEntity> findByPaymentIntentId(String paymentIntentId);
    Page<OrderEntity> findByUserId(UUID userId, Pageable pageable);
    Page<OrderEntity> findByStatus(OrderStatus status, Pageable pageable);

    @Query("select coalesce(sum(o.itemsTotalCents + o.shippingCents), 0) from OrderEntity o where o.status in :statuses")
    long sumRevenue(List<OrderStatus> statuses);

    @Query("select o.status, count(o) from OrderEntity o group by o.status")
    List<Object[]> countGroupedByStatus();
}
