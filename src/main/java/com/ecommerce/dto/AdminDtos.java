package com.ecommerce.dto;
import jakarta.validation.constraints.*;
public final class AdminDtos { private AdminDtos() {} public record InventoryAdjustmentRequest(@NotNull Integer quantityChange,@NotBlank String reason) {} public record DashboardResponse(long users,long products,long orders,long payments) {} }