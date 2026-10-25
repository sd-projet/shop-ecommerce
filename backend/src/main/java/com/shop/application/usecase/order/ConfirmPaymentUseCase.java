package com.shop.application.usecase.order;

import com.shop.application.port.EmailSender;
import com.shop.application.port.PaymentGateway;
import com.shop.domain.exception.AccessDeniedDomainException;
import com.shop.domain.exception.DomainException;
import com.shop.domain.exception.ResourceNotFoundException;
import com.shop.domain.model.*;
import com.shop.domain.repository.OrderRepository;
import com.shop.domain.repository.ProductRepository;
import com.shop.domain.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/** Marks an order as PAID once the provider confirms the payment (client callback or webhook). Idempotent. */
@Service
public class ConfirmPaymentUseCase {

    private final OrderRepository orders;
    private final ProductRepository products;
    private final UserRepository users;
    private final PaymentGateway payments;
    private final EmailSender emails;

    public ConfirmPaymentUseCase(OrderRepository orders, ProductRepository products, UserRepository users,
                                 PaymentGateway payments, EmailSender emails) {
        this.orders = orders;
        this.products = products;
        this.users = users;
        this.payments = payments;
        this.emails = emails;
    }

    /** Called by the authenticated customer after the payment form succeeded: the provider is queried, never trusted. */
    @Transactional
    public Order confirmForCustomer(UUID orderId, UUID userId) {
        Order order = orders.findById(orderId).orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        if (!order.belongsTo(userId)) {
            throw new AccessDeniedDomainException("This order does not belong to you");
        }
        if (order.isPaid()) {
            return order;
        }
        if (!payments.isPaymentSucceeded(order.getPaymentIntentId())) {
            throw new DomainException("The payment has not been completed yet");
        }
        return markPaid(order);
    }

    /** Called by the provider webhook (signature already verified by the gateway). */
    @Transactional
    public void confirmFromWebhook(String paymentIntentId) {
        orders.findByPaymentIntentId(paymentIntentId)
                .filter(o -> !o.isPaid())
                .filter(o -> o.getStatus() == OrderStatus.PENDING)
                .ifPresent(this::markPaid);
    }

    private Order markPaid(Order order) {
        order.changeStatus(OrderStatus.PAID);
        for (OrderItem item : order.getItems()) {
            products.findById(item.productId()).ifPresent(p -> {
                p.recordSale(item.quantity());
                products.save(p);
            });
        }
        Order saved = orders.save(order);
        users.findById(saved.getUserId()).ifPresent(user -> emails.sendOrderConfirmation(user, saved));
        return saved;
    }
}
