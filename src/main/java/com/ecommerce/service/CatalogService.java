package com.ecommerce.service;

import com.ecommerce.dto.CatalogDtos.*;
import com.ecommerce.exception.ApiException;
import com.ecommerce.model.Category;
import com.ecommerce.model.Product;
import com.ecommerce.repository.CategoryRepository;
import com.ecommerce.repository.ProductRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CatalogService {
    private final CategoryRepository categories;
    private final ProductRepository products;
    private final SlugService slugs;

    public CatalogService(CategoryRepository categories, ProductRepository products, SlugService slugs) {
        this.categories = categories;
        this.products = products;
        this.slugs = slugs;
    }

    @Transactional(readOnly = true)
    @Cacheable("categories")
    public List<CategoryResponse> categories() {
        return categories.findAll().stream().map(this::toCategoryResponse).toList();
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "products", key = "#categorySlug ?: 'all'")
    public List<ProductResponse> products(String categorySlug) {
        List<Product> found = categorySlug == null || categorySlug.isBlank()
                ? products.findByActiveTrue()
                : products.findByActiveTrueAndCategorySlug(categorySlug);
        return found.stream().map(this::toProductResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> search(String keyword) {
        return products.findByNameContainingIgnoreCaseAndActiveTrue(keyword).stream().map(this::toProductResponse).toList();
    }

    @Transactional(readOnly = true)
    public ProductResponse product(Long id) {
        Product product = products.findById(id).filter(Product::isActive).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Product not found"));
        return toProductResponse(product);
    }

    @Transactional
    @CacheEvict(value = {"products", "categories"}, allEntries = true)
    public CategoryResponse createCategory(CategoryRequest request) {
        String slug = slugs.slugify(request.name());
        if (categories.existsBySlug(slug)) {
            throw new ApiException(HttpStatus.CONFLICT, "Category already exists");
        }
        Category category = new Category();
        category.setName(request.name());
        category.setSlug(slug);
        category.setDescription(request.description());
        return toCategoryResponse(categories.save(category));
    }

    @Transactional
    @CacheEvict(value = "products", allEntries = true)
    public ProductResponse createProduct(ProductRequest request) {
        if (products.existsBySku(request.sku())) {
            throw new ApiException(HttpStatus.CONFLICT, "SKU already exists");
        }
        Product product = new Product();
        apply(product, request);
        return toProductResponse(products.save(product));
    }

    @Transactional
    @CacheEvict(value = "products", allEntries = true)
    public ProductResponse updateProduct(Long id, ProductRequest request) {
        Product product = products.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Product not found"));
        products.findBySku(request.sku()).filter(existing -> !existing.getId().equals(id)).ifPresent(existing -> { throw new ApiException(HttpStatus.CONFLICT, "SKU already exists"); });
        apply(product, request);
        return toProductResponse(product);
    }

    @Transactional
    @CacheEvict(value = "products", allEntries = true)
    public void deleteProduct(Long id) {
        Product product = products.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Product not found"));
        product.setActive(false);
    }

    private void apply(Product product, ProductRequest request) {
        Category category = categories.findById(request.categoryId()).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Category not found"));
        product.setName(request.name());
        product.setSku(request.sku());
        product.setDescription(request.description());
        product.setImageUrl(request.imageUrl());
        product.setPrice(request.price());
        product.setStockQuantity(request.stockQuantity());
        product.setCategory(category);
        product.setActive(request.active());
    }

    public CategoryResponse toCategoryResponse(Category category) {
        return new CategoryResponse(category.getId(), category.getName(), category.getSlug(), category.getDescription());
    }

    public ProductResponse toProductResponse(Product product) {
        return new ProductResponse(product.getId(), product.getName(), product.getSku(), product.getDescription(), product.getImageUrl(), product.getPrice(), product.getStockQuantity(), product.isActive(), toCategoryResponse(product.getCategory()));
    }
}
