package com.ecommerce.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public final class CatalogDtos {
    private CatalogDtos() {}
    public record CategoryRequest(@NotBlank String name, String description) {}
    public record CategoryResponse(Long id, String name, String slug, String description) {}
    public record ProductRequest(@NotBlank String name, @NotBlank String sku, String description, String imageUrl, @NotNull @DecimalMin("0.01") BigDecimal price, @NotNull @Min(0) Integer stockQuantity, @NotNull Long categoryId, boolean active) {}
    public record ProductResponse(Long id, String name, String sku, String description, String imageUrl, BigDecimal price, Integer stockQuantity, boolean active, CategoryResponse category) {}
}
