package com.shop.presentation.dto;

import com.shop.domain.model.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Request / response payloads of the REST API. */
public final class Dtos {

    private Dtos() {}

    // ---------- Auth & profile ----------
    public record RegisterRequest(
            @NotBlank @Email String email,
            @NotBlank @Size(min = 8, max = 72) String password,
            @NotBlank @Size(max = 100) String firstName,
            @NotBlank @Size(max = 100) String lastName) {}

    public record LoginRequest(@NotBlank @Email String email, @NotBlank String password) {}

    public record UpdateProfileRequest(@NotBlank @Size(max = 100) String firstName, @NotBlank @Size(max = 100) String lastName) {}

    public record UserResponse(UUID id, String email, String firstName, String lastName, Role role) {
        public static UserResponse from(User u) {
            return new UserResponse(u.getId(), u.getEmail(), u.getFirstName(), u.getLastName(), u.getRole());
        }
    }

    public record AuthResponse(String token, UserResponse user) {}

    // ---------- Catalogue ----------
    public record ProductRequest(
            @NotBlank @Size(max = 200) String name,
            @Size(max = 5000) String description,
            @PositiveOrZero long priceCents,
            @Size(max = 500) String imageUrl,
            @NotBlank @Size(max = 100) String category,
            @PositiveOrZero int stock) {}

    public record StockRequest(@PositiveOrZero int stock) {}

    public record ProductResponse(UUID id, String name, String description, long priceCents, String imageUrl,
                                  String category, int stock, int salesCount, boolean archived) {
        public static ProductResponse from(Product p) {
            return new ProductResponse(p.getId(), p.getName(), p.getDescription(), p.getPriceCents(), p.getImageUrl(),
                    p.getCategory(), p.getStock(), p.getSalesCount(), p.isArchived());
        }
    }

    public record PageResponse<T>(List<T> content, int page, int size, long totalElements, int totalPages) {
        public static <S, T> PageResponse<T> from(PageResult<S> page, java.util.function.Function<S, T> mapper) {
            PageResult<T> mapped = page.map(mapper);
            return new PageResponse<>(mapped.content(), mapped.page(), mapped.size(), mapped.totalElements(), mapped.totalPages());
        }
    }

    // ---------- Orders ----------
    public record AddressDto(
            @NotBlank String fullName,
            @NotBlank String street,
            @NotBlank String city,
            @NotBlank @Size(max = 20) String postalCode,
            @NotBlank String country) {
        public Address toDomain() {
            return new Address(fullName, street, city, postalCode, country);
        }
        public static AddressDto from(Address a) {
            return new AddressDto(a.fullName(), a.street(), a.city(), a.postalCode(), a.country());
        }
    }

    public record CartLineDto(@NotNull UUID productId, @Min(1) @Max(100) int quantity) {}

    public record CheckoutRequest(
            @NotEmpty @Valid List<CartLineDto> items,
            @NotNull @Valid AddressDto address,
            @NotNull ShippingMethod shippingMethod) {}

    public record OrderItemResponse(UUID productId, String productName, long unitPriceCents, int quantity) {}

    public record OrderResponse(UUID id, UUID userId, OrderStatus status, List<OrderItemResponse> items, AddressDto address,
                                ShippingMethod shippingMethod, long itemsTotalCents, long shippingCents, long totalCents,
                                Instant createdAt, Instant updatedAt) {
        public static OrderResponse from(Order o) {
            return new OrderResponse(o.getId(), o.getUserId(), o.getStatus(),
                    o.getItems().stream().map(i -> new OrderItemResponse(i.productId(), i.productName(), i.unitPriceCents(), i.quantity())).toList(),
                    AddressDto.from(o.getShippingAddress()), o.getShippingMethod(), o.itemsTotalCents(), o.shippingCents(),
                    o.totalCents(), o.getCreatedAt(), o.getUpdatedAt());
        }
    }

    public record CheckoutResponse(OrderResponse order, String clientSecret) {}

    public record UpdateOrderStatusRequest(@NotNull OrderStatus status) {}

    // ---------- Admin ----------
    public record DashboardResponse(long revenueCents, long totalOrders, Map<OrderStatus, Long> ordersByStatus,
                                    long activeProducts, long lowStockProducts, long customers) {
        public static DashboardResponse from(SalesStats s) {
            return new DashboardResponse(s.revenueCents(), s.totalOrders(), s.ordersByStatus(), s.activeProducts(),
                    s.lowStockProducts(), s.customers());
        }
    }
}
