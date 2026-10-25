package com.shop.domain;

import com.shop.domain.exception.DomainException;
import com.shop.domain.exception.InsufficientStockException;
import com.shop.domain.model.Product;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProductTest {

    private Product product(int stock) {
        return Product.create("Casque", "desc", 1000, null, "Audio", stock);
    }

    @Test
    void reserveDecreasesStock() {
        Product p = product(10);
        p.reserve(3);
        assertThat(p.getStock()).isEqualTo(7);
    }

    @Test
    void reserveFailsWhenStockIsInsufficient() {
        Product p = product(2);
        assertThatThrownBy(() -> p.reserve(3)).isInstanceOf(InsufficientStockException.class);
        assertThat(p.getStock()).isEqualTo(2);
    }

    @Test
    void archivedProductCannotBeReserved() {
        Product p = product(5);
        p.archive();
        assertThatThrownBy(() -> p.reserve(1)).isInstanceOf(DomainException.class);
    }

    @Test
    void releaseGivesStockBack() {
        Product p = product(5);
        p.reserve(5);
        p.release(2);
        assertThat(p.getStock()).isEqualTo(2);
    }

    @Test
    void negativePriceIsRejected() {
        assertThatThrownBy(() -> Product.create("x", "d", -1, null, "c", 1)).isInstanceOf(DomainException.class);
    }
}
