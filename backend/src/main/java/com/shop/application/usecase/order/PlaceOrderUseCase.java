package com.shop.application.usecase.order;

import com.shop.application.port.PaymentGateway;
import com.shop.domain.exception.DomainException;
import com.shop.domain.exception.ResourceNotFoundException;
import com.shop.domain.model.*;
import com.shop.domain.repository.OrderRepository;
import com.shop.domain.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Checkout: validates the cart, reserves stock, creates the order (PENDING) and a payment intent.
 * The order only becomes PAID once the payment provider confirms it (see {@link ConfirmPaymentUseCase}).
 */
@Service
public class PlaceOrderUseCase {

    public record Line(UUID productId, int quantity) {}

    public record Command(UUID userId, List<Line> lines, Address address, ShippingMethod shippingMethod) {}

    public record Result(Order order, String clientSecret) {}

    private static final String CURRENCY = "eur";

    private final ProductRepository products;
    private final OrderRepository orders;
    private final PaymentGateway payments;

    public PlaceOrderUseCase(ProductRepository products, OrderRepository orders, PaymentGateway payments) {
        this.products = products;
        this.orders = orders;
        this.payments = payments;
    }

    @Transactional
    public Result execute(Command command) {
        if (command.lines() == null || command.lines().isEmpty()) {
            throw new DomainException("The cart is empty");
        }
        // merge duplicated lines
        Map<UUID, Integer> quantities = command.lines().stream()
                .collect(Collectors.toMap(Line::productId, Line::quantity, Integer::sum, LinkedHashMap::new));

        Map<UUID, Product> locked = products.findAllByIdForUpdate(quantities.keySet()).stream()
                .collect(Collectors.toMap(Product::getId, Function.identity()));

        List<OrderItem> items = new ArrayList<>();
        for (var entry : quantities.entrySet()) {
            Product product = locked.get(entry.getKey());
            if (product == null) {
                throw new ResourceNotFoundException("Product not found: " + entry.getKey());
            }
            product.reserve(entry.getValue());
            products.save(product);
            items.add(new OrderItem(product.getId(), product.getName(), product.getPriceCents(), entry.getValue()));
        }

        Order order = Order.place(command.userId(), items, command.address(), command.shippingMethod());
        PaymentGateway.PaymentIntentInfo intent = payments.createPaymentIntent(order.totalCents(), CURRENCY, order.getId());
        order.attachPaymentIntent(intent.id());
        Order saved = orders.save(order);
        return new Result(saved, intent.clientSecret());
    }
}
