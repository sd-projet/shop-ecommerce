package com.shop.application;

import com.shop.application.port.EmailSender;
import com.shop.application.port.PaymentGateway;
import com.shop.application.usecase.order.ConfirmPaymentUseCase;
import com.shop.domain.exception.AccessDeniedDomainException;
import com.shop.domain.exception.DomainException;
import com.shop.domain.model.*;
import com.shop.domain.repository.OrderRepository;
import com.shop.domain.repository.ProductRepository;
import com.shop.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConfirmPaymentUseCaseTest {

    @Mock OrderRepository orders;
    @Mock ProductRepository products;
    @Mock UserRepository users;
    @Mock PaymentGateway payments;
    @Mock EmailSender emails;

    ConfirmPaymentUseCase useCase;
    Order order;
    UUID userId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        useCase = new ConfirmPaymentUseCase(orders, products, users, payments, emails);
        order = Order.place(userId, List.of(new OrderItem(UUID.randomUUID(), "A", 1000, 1)),
                new Address("Jane", "1 rue", "Paris", "75001", "FR"), ShippingMethod.PICKUP);
        order.attachPaymentIntent("pi_1");
        when(orders.findById(order.getId())).thenReturn(Optional.of(order));
    }

    @Test
    void marksOrderPaidAndSendsEmailWhenProviderConfirms() {
        User user = User.create("jane@example.com", "h", "Jane", "Doe", Role.ROLE_CUSTOMER);
        when(payments.isPaymentSucceeded("pi_1")).thenReturn(true);
        when(orders.save(any())).thenAnswer(i -> i.getArgument(0));
        when(products.findById(any())).thenReturn(Optional.empty());
        when(users.findById(userId)).thenReturn(Optional.of(user));

        Order result = useCase.confirmForCustomer(order.getId(), userId);

        assertThat(result.getStatus()).isEqualTo(OrderStatus.PAID);
        verify(emails).sendOrderConfirmation(user, result);
    }

    @Test
    void refusesWhenProviderHasNotConfirmed() {
        when(payments.isPaymentSucceeded("pi_1")).thenReturn(false);

        assertThatThrownBy(() -> useCase.confirmForCustomer(order.getId(), userId)).isInstanceOf(DomainException.class);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING);
        verify(emails, never()).sendOrderConfirmation(any(), any());
    }

    @Test
    void refusesAnotherCustomersOrder() {
        assertThatThrownBy(() -> useCase.confirmForCustomer(order.getId(), UUID.randomUUID()))
                .isInstanceOf(AccessDeniedDomainException.class);
    }
}
