package com.ecommerce.dto;

import com.ecommerce.model.OrderStatus;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public final class OrderDtos {
    private OrderDtos() {}
    public record CheckoutRequest(@NotBlank String shippingAddress) {}
    public record OrderStatusRequest(@NotNull OrderStatus status) {}
    public record OrderItemResponse(Long productId, String productName, BigDecimal unitPrice, Integer quantity, BigDecimal subtotal) {}
    public record OrderResponse(Long id, Long customerId, String customerName, String customerEmail, OrderStatus status, BigDecimal totalAmount, String shippingAddress, Instant createdAt, List<OrderItemResponse> items) {}
}
