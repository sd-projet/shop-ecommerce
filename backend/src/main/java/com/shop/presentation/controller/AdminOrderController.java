package com.shop.presentation.controller;

import com.shop.application.usecase.admin.GetDashboardStatsUseCase;
import com.shop.application.usecase.order.OrderQueryUseCase;
import com.shop.application.usecase.order.UpdateOrderStatusUseCase;
import com.shop.domain.model.OrderStatus;
import com.shop.presentation.dto.Dtos.*;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
public class AdminOrderController {

    private final OrderQueryUseCase queries;
    private final UpdateOrderStatusUseCase updateStatus;
    private final GetDashboardStatsUseCase dashboard;

    public AdminOrderController(OrderQueryUseCase queries, UpdateOrderStatusUseCase updateStatus, GetDashboardStatsUseCase dashboard) {
        this.queries = queries;
        this.updateStatus = updateStatus;
        this.dashboard = dashboard;
    }

    @GetMapping("/dashboard")
    public DashboardResponse dashboard() {
        return DashboardResponse.from(dashboard.execute());
    }

    @GetMapping("/orders")
    public PageResponse<OrderResponse> orders(@RequestParam(required = false) OrderStatus status,
                                              @RequestParam(defaultValue = "0") int page,
                                              @RequestParam(defaultValue = "20") int size) {
        return PageResponse.from(queries.allOrders(status, page, size), OrderResponse::from);
    }

    @PatchMapping("/orders/{id}/status")
    public OrderResponse updateStatus(@PathVariable UUID id, @Valid @RequestBody UpdateOrderStatusRequest request) {
        return OrderResponse.from(updateStatus.execute(id, request.status()));
    }
}
