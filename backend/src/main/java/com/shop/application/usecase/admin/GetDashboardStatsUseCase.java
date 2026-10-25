package com.shop.application.usecase.admin;

import com.shop.domain.model.OrderStatus;
import com.shop.domain.model.Role;
import com.shop.domain.model.SalesStats;
import com.shop.domain.repository.OrderRepository;
import com.shop.domain.repository.ProductRepository;
import com.shop.domain.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumMap;
import java.util.Map;

@Service
@Transactional(readOnly = true)
public class GetDashboardStatsUseCase {

    static final int LOW_STOCK_THRESHOLD = 5;

    private final OrderRepository orders;
    private final ProductRepository products;
    private final UserRepository users;

    public GetDashboardStatsUseCase(OrderRepository orders, ProductRepository products, UserRepository users) {
        this.orders = orders;
        this.products = products;
        this.users = users;
    }

    public SalesStats execute() {
        Map<OrderStatus, Long> byStatus = new EnumMap<>(OrderStatus.class);
        for (OrderStatus s : OrderStatus.values()) byStatus.put(s, 0L);
        byStatus.putAll(orders.countByStatus());
        long total = byStatus.values().stream().mapToLong(Long::longValue).sum();
        return new SalesStats(orders.sumRevenueCents(), total, byStatus,
                products.countActive(), products.countLowStock(LOW_STOCK_THRESHOLD), users.countByRole(Role.ROLE_CUSTOMER));
    }
}
