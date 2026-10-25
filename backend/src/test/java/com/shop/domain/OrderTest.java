package com.shop.domain;

import com.shop.domain.exception.DomainException;
import com.shop.domain.exception.InvalidOrderStateException;
import com.shop.domain.model.*;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrderTest {

    private static final Address ADDRESS = new Address("Jane Doe", "1 rue de Paris", "Paris", "75001", "FR");

    private Order order() {
        return Order.place(UUID.randomUUID(),
                List.of(new OrderItem(UUID.randomUUID(), "A", 1000, 2), new OrderItem(UUID.randomUUID(), "B", 500, 1)),
                ADDRESS, ShippingMethod.STANDARD);
    }

    @Test
    void totalIncludesShipping() {
        Order o = order();
        assertThat(o.itemsTotalCents()).isEqualTo(2500);
        assertThat(o.totalCents()).isEqualTo(2500 + ShippingMethod.STANDARD.costCents());
        assertThat(o.getStatus()).isEqualTo(OrderStatus.PENDING);
    }

    @Test
    void happyPathLifecycle() {
        Order o = order();
        o.changeStatus(OrderStatus.PAID);
        o.changeStatus(OrderStatus.SHIPPED);
        o.changeStatus(OrderStatus.DELIVERED);
        assertThat(o.getStatus()).isEqualTo(OrderStatus.DELIVERED);
    }

    @Test
    void cannotShipAnUnpaidOrder() {
        Order o = order();
        assertThatThrownBy(() -> o.changeStatus(OrderStatus.SHIPPED)).isInstanceOf(InvalidOrderStateException.class);
    }

    @Test
    void cannotCancelAPaidOrder() {
        Order o = order();
        o.changeStatus(OrderStatus.PAID);
        assertThatThrownBy(() -> o.changeStatus(OrderStatus.CANCELLED)).isInstanceOf(InvalidOrderStateException.class);
    }

    @Test
    void emptyOrderIsRejected() {
        assertThatThrownBy(() -> Order.place(UUID.randomUUID(), List.of(), ADDRESS, ShippingMethod.PICKUP))
                .isInstanceOf(DomainException.class);
    }
}
