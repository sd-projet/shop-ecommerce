package com.shop.application;

import com.shop.application.port.PaymentGateway;
import com.shop.application.usecase.order.PlaceOrderUseCase;
import com.shop.domain.exception.InsufficientStockException;
import com.shop.domain.exception.ResourceNotFoundException;
import com.shop.domain.model.*;
import com.shop.domain.repository.OrderRepository;
import com.shop.domain.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlaceOrderUseCaseTest {

    private static final Address ADDRESS = new Address("Jane Doe", "1 rue de Paris", "Paris", "75001", "FR");

    @Mock ProductRepository products;
    @Mock OrderRepository orders;
    @Mock PaymentGateway payments;
    @InjectMocks PlaceOrderUseCase useCase;

    @Test
    void createsPendingOrderReservesStockAndCreatesPaymentIntent() {
        Product product = Product.create("Casque", "d", 1000, null, "Audio", 10);
        when(products.findAllByIdForUpdate(any())).thenReturn(List.of(product));
        when(products.save(any())).thenAnswer(i -> i.getArgument(0));
        when(orders.save(any())).thenAnswer(i -> i.getArgument(0));
        when(payments.createPaymentIntent(anyLong(), eq("eur"), any()))
                .thenReturn(new PaymentGateway.PaymentIntentInfo("pi_1", "secret_1"));

        var result = useCase.execute(new PlaceOrderUseCase.Command(UUID.randomUUID(),
                List.of(new PlaceOrderUseCase.Line(product.getId(), 2)), ADDRESS, ShippingMethod.STANDARD));

        assertThat(result.order().getStatus()).isEqualTo(OrderStatus.PENDING);
        assertThat(result.order().totalCents()).isEqualTo(2 * 1000 + ShippingMethod.STANDARD.costCents());
        assertThat(result.order().getPaymentIntentId()).isEqualTo("pi_1");
        assertThat(result.clientSecret()).isEqualTo("secret_1");
        assertThat(product.getStock()).isEqualTo(8);
        verify(payments).createPaymentIntent(2000 + ShippingMethod.STANDARD.costCents(), "eur", result.order().getId());
    }

    @Test
    void failsWhenStockIsInsufficient() {
        Product product = Product.create("Casque", "d", 1000, null, "Audio", 1);
        when(products.findAllByIdForUpdate(any())).thenReturn(List.of(product));

        assertThatThrownBy(() -> useCase.execute(new PlaceOrderUseCase.Command(UUID.randomUUID(),
                List.of(new PlaceOrderUseCase.Line(product.getId(), 5)), ADDRESS, ShippingMethod.EXPRESS)))
                .isInstanceOf(InsufficientStockException.class);

        verify(orders, never()).save(any());
        verify(payments, never()).createPaymentIntent(anyLong(), any(), any());
    }

    @Test
    void failsWhenProductDoesNotExist() {
        when(products.findAllByIdForUpdate(any())).thenReturn(List.of());

        assertThatThrownBy(() -> useCase.execute(new PlaceOrderUseCase.Command(UUID.randomUUID(),
                List.of(new PlaceOrderUseCase.Line(UUID.randomUUID(), 1)), ADDRESS, ShippingMethod.PICKUP)))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
