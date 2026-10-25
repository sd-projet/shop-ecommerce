package com.shop.presentation.controller;

import com.shop.application.usecase.order.*;
import com.shop.infrastructure.security.AuthenticatedUser;
import com.shop.presentation.dto.Dtos.*;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final PlaceOrderUseCase placeOrder;
    private final ConfirmPaymentUseCase confirmPayment;
    private final CancelOrderUseCase cancelOrder;
    private final OrderQueryUseCase queries;

    public OrderController(PlaceOrderUseCase placeOrder, ConfirmPaymentUseCase confirmPayment,
                           CancelOrderUseCase cancelOrder, OrderQueryUseCase queries) {
        this.placeOrder = placeOrder;
        this.confirmPayment = confirmPayment;
        this.cancelOrder = cancelOrder;
        this.queries = queries;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CheckoutResponse checkout(@AuthenticationPrincipal AuthenticatedUser user, @Valid @RequestBody CheckoutRequest request) {
        var lines = request.items().stream().map(i -> new PlaceOrderUseCase.Line(i.productId(), i.quantity())).toList();
        var result = placeOrder.execute(new PlaceOrderUseCase.Command(user.id(), lines, request.address().toDomain(), request.shippingMethod()));
        return new CheckoutResponse(OrderResponse.from(result.order()), result.clientSecret());
    }

    @GetMapping
    public PageResponse<OrderResponse> myOrders(@AuthenticationPrincipal AuthenticatedUser user,
                                                @RequestParam(defaultValue = "0") int page,
                                                @RequestParam(defaultValue = "10") int size) {
        return PageResponse.from(queries.myOrders(user.id(), page, size), OrderResponse::from);
    }

    @GetMapping("/{id}")
    public OrderResponse get(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable UUID id) {
        return OrderResponse.from(queries.getForUser(id, user.id(), user.isAdmin()));
    }

    @PostMapping("/{id}/confirm-payment")
    public OrderResponse confirmPayment(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable UUID id) {
        return OrderResponse.from(confirmPayment.confirmForCustomer(id, user.id()));
    }

    @PostMapping("/{id}/cancel")
    public OrderResponse cancel(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable UUID id) {
        return OrderResponse.from(cancelOrder.executeForCustomer(id, user.id()));
    }
}
