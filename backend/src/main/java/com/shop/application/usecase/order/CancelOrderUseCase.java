package com.shop.application.usecase.order;

import com.shop.domain.exception.AccessDeniedDomainException;
import com.shop.domain.exception.ResourceNotFoundException;
import com.shop.domain.model.Order;
import com.shop.domain.model.OrderItem;
import com.shop.domain.model.OrderStatus;
import com.shop.domain.model.Product;
import com.shop.domain.repository.OrderRepository;
import com.shop.domain.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/** Cancels an unpaid order and gives the reserved stock back. */
@Service
public class CancelOrderUseCase {

    private final OrderRepository orders;
    private final ProductRepository products;

    public CancelOrderUseCase(OrderRepository orders, ProductRepository products) {
        this.orders = orders;
        this.products = products;
    }

    @Transactional
    public Order executeForCustomer(UUID orderId, UUID userId) {
        Order order = orders.findById(orderId).orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        if (!order.belongsTo(userId)) {
            throw new AccessDeniedDomainException("This order does not belong to you");
        }
        return cancel(order);
    }

    /** Shared with the back-office use case; runs inside the caller's transaction. */
    public Order cancel(Order order) {
        order.changeStatus(OrderStatus.CANCELLED);
        List<UUID> ids = order.getItems().stream().map(OrderItem::productId).toList();
        for (Product product : products.findAllByIdForUpdate(ids)) {
            order.getItems().stream()
                    .filter(i -> i.productId().equals(product.getId()))
                    .forEach(i -> product.release(i.quantity()));
            products.save(product);
        }
        return orders.save(order);
    }
}
