package com.shop.application.usecase.order;

import com.shop.domain.exception.AccessDeniedDomainException;
import com.shop.domain.exception.ResourceNotFoundException;
import com.shop.domain.model.Order;
import com.shop.domain.model.OrderStatus;
import com.shop.domain.model.PageResult;
import com.shop.domain.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class OrderQueryUseCase {

    private final OrderRepository orders;

    public OrderQueryUseCase(OrderRepository orders) {
        this.orders = orders;
    }

    public PageResult<Order> myOrders(UUID userId, int page, int size) {
        return orders.findByUserId(userId, page, size);
    }

    public Order getForUser(UUID orderId, UUID requesterId, boolean admin) {
        Order order = orders.findById(orderId).orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        if (!admin && !order.belongsTo(requesterId)) {
            throw new AccessDeniedDomainException("This order does not belong to you");
        }
        return order;
    }

    public PageResult<Order> allOrders(OrderStatus status, int page, int size) {
        return orders.findAll(status, page, size);
    }
}
