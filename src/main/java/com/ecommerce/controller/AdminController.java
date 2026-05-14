package com.ecommerce.controller;

import com.ecommerce.dto.AdminDtos.DashboardResponse;
import com.ecommerce.dto.AdminDtos.InventoryAdjustmentRequest;
import com.ecommerce.dto.CatalogDtos.CategoryRequest;
import com.ecommerce.dto.CatalogDtos.CategoryResponse;
import com.ecommerce.dto.CatalogDtos.ProductRequest;
import com.ecommerce.dto.CatalogDtos.ProductResponse;
import com.ecommerce.dto.OrderDtos.OrderResponse;
import com.ecommerce.dto.OrderDtos.OrderStatusRequest;
import com.ecommerce.repository.OrderRepository;
import com.ecommerce.repository.PaymentRepository;
import com.ecommerce.repository.ProductRepository;
import com.ecommerce.repository.UserRepository;
import com.ecommerce.service.CatalogService;
import com.ecommerce.service.InventoryService;
import com.ecommerce.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    private final CatalogService catalog;
    private final InventoryService inventory;
    private final OrderService orders;
    private final UserRepository users;
    private final ProductRepository products;
    private final OrderRepository orderRepo;
    private final PaymentRepository payments;

    public AdminController(CatalogService catalog, InventoryService inventory, OrderService orders, UserRepository users, ProductRepository products, OrderRepository orderRepo, PaymentRepository payments) {
        this.catalog = catalog;
        this.inventory = inventory;
        this.orders = orders;
        this.users = users;
        this.products = products;
        this.orderRepo = orderRepo;
        this.payments = payments;
    }

    @GetMapping("/dashboard")
    public DashboardResponse dashboard() {
        return new DashboardResponse(users.count(), products.count(), orderRepo.count(), payments.count());
    }

    @PostMapping("/categories")
    public CategoryResponse createCategory(@Valid @RequestBody CategoryRequest request) {
        return catalog.createCategory(request);
    }

    @PostMapping("/products")
    public ProductResponse createProduct(@Valid @RequestBody ProductRequest request) {
        return catalog.createProduct(request);
    }

    @PutMapping("/products/{id}")
    public ProductResponse updateProduct(@PathVariable Long id, @Valid @RequestBody ProductRequest request) {
        return catalog.updateProduct(id, request);
    }

    @DeleteMapping("/products/{id}")
    public void deleteProduct(@PathVariable Long id) {
        catalog.deleteProduct(id);
    }

    @PatchMapping("/products/{id}/inventory")
    public ProductResponse adjustInventory(@PathVariable Long id, @Valid @RequestBody InventoryAdjustmentRequest request) {
        return inventory.adjust(id, request);
    }

    @GetMapping("/orders")
    public List<OrderResponse> allOrders() {
        return orders.allOrders();
    }

    @PatchMapping("/orders/{id}/status")
    public OrderResponse updateStatus(@PathVariable Long id, @Valid @RequestBody OrderStatusRequest request) {
        return orders.updateStatus(id, request.status());
    }
}
