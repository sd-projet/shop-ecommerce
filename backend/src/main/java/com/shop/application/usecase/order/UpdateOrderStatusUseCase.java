package com.shop.application.usecase.order;

import com.shop.domain.exception.InvalidOrderStateException;
import com.shop.domain.exception.ResourceNotFoundException;
import com.shop.domain.model.Order;
import com.shop.domain.model.OrderStatus;
import com.shop.domain.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/** Back-office: ships / delivers / cancels an order. PAID is only reachable through the payment flow. */
@Service
public class UpdateOrderStatusUseCase {

    private final OrderRepository orders;
    private final CancelOrderUseCase cancelOrder;

    public UpdateOrderStatusUseCase(OrderRepository orders, CancelOrderUseCase cancelOrder) {
        this.orders = orders;
        this.cancelOrder = cancelOrder;
    }

    @Transactional
    public Order execute(UUID orderId, OrderStatus target) {
        Order order = orders.findById(orderId).orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        return switch (target) {
            case PAID, PENDING -> throw new InvalidOrderStateException("Status " + target + " cannot be set manually");
            case CANCELLED -> cancelOrder.cancel(order);
            case SHIPPED, DELIVERED -> {
                order.changeStatus(target);
                yield orders.save(order);
            }
        };
    }
}
