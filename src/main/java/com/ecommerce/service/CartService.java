package com.ecommerce.service;

import com.ecommerce.dto.CartDtos.AddToCartRequest;
import com.ecommerce.dto.CartDtos.CartItemResponse;
import com.ecommerce.dto.CartDtos.CartResponse;
import com.ecommerce.dto.CartDtos.UpdateCartItemRequest;
import com.ecommerce.exception.ApiException;
import com.ecommerce.model.CartItem;
import com.ecommerce.model.Product;
import com.ecommerce.model.User;
import com.ecommerce.repository.CartItemRepository;
import com.ecommerce.repository.ProductRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Service
public class CartService {
    private final CartItemRepository cartItems;
    private final ProductRepository products;

    public CartService(CartItemRepository cartItems, ProductRepository products) {
        this.cartItems = cartItems;
        this.products = products;
    }

    @Transactional(readOnly = true)
    public CartResponse getCart(User user) {
        List<CartItemResponse> items = cartItems.findByUser(user).stream().map(this::toResponse).toList();
        BigDecimal total = items.stream().map(CartItemResponse::subtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
        return new CartResponse(items, total);
    }

    @Transactional
    @CacheEvict(value = "products", allEntries = true)
    public CartResponse add(User user, AddToCartRequest request) {
        Product product = products.findById(request.productId())
                .filter(Product::isActive)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Product not found"));
        ensureAvailable(product, request.quantity());

        CartItem item = cartItems.findByUserAndProduct(user, product).orElseGet(CartItem::new);
        item.setUser(user);
        item.setProduct(product);
        item.setQuantity((item.getId() == null ? 0 : item.getQuantity()) + request.quantity());
        item.setUpdatedAt(Instant.now());

        product.setStockQuantity(product.getStockQuantity() - request.quantity());
        products.save(product);
        cartItems.save(item);
        return getCart(user);
    }

    @Transactional
    @CacheEvict(value = "products", allEntries = true)
    public CartResponse update(User user, Long itemId, UpdateCartItemRequest request) {
        CartItem item = cartItems.findById(itemId)
                .filter(found -> found.getUser().getId().equals(user.getId()))
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Cart item not found"));

        Product product = item.getProduct();
        int delta = request.quantity() - item.getQuantity();
        if (delta > 0) {
            ensureAvailable(product, delta);
            product.setStockQuantity(product.getStockQuantity() - delta);
        } else if (delta < 0) {
            product.setStockQuantity(product.getStockQuantity() + Math.abs(delta));
        }
        products.save(product);
        item.setQuantity(request.quantity());
        item.setUpdatedAt(Instant.now());
        return getCart(user);
    }

    @Transactional
    @CacheEvict(value = "products", allEntries = true)
    public void remove(User user, Long itemId) {
        CartItem item = cartItems.findById(itemId)
                .filter(found -> found.getUser().getId().equals(user.getId()))
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Cart item not found"));
        Product product = item.getProduct();
        product.setStockQuantity(product.getStockQuantity() + item.getQuantity());
        products.save(product);
        cartItems.delete(item);
    }

    private void ensureAvailable(Product product, int quantity) {
        if (product.getStockQuantity() < quantity) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Only " + product.getStockQuantity() + " item(s) available for " + product.getName());
        }
    }

    private CartItemResponse toResponse(CartItem item) {
        BigDecimal subtotal = item.getProduct().getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
        return new CartItemResponse(item.getId(), item.getProduct().getId(), item.getProduct().getName(), item.getProduct().getPrice(), item.getQuantity(), subtotal);
    }
}
