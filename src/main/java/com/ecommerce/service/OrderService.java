package com.ecommerce.service;

import com.ecommerce.dto.OrderDtos.CheckoutRequest;
import com.ecommerce.dto.OrderDtos.OrderItemResponse;
import com.ecommerce.dto.OrderDtos.OrderResponse;
import com.ecommerce.exception.ApiException;
import com.ecommerce.model.CartItem;
import com.ecommerce.model.Order;
import com.ecommerce.model.OrderItem;
import com.ecommerce.model.OrderStatus;
import com.ecommerce.model.Product;
import com.ecommerce.model.User;
import com.ecommerce.repository.CartItemRepository;
import com.ecommerce.repository.OrderRepository;
import com.ecommerce.repository.ProductRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class OrderService {
    private final OrderRepository orders;
    private final CartItemRepository cartItems;
    private final ProductRepository products;

    public OrderService(OrderRepository orders, CartItemRepository cartItems, ProductRepository products) {
        this.orders = orders;
        this.cartItems = cartItems;
        this.products = products;
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> myOrders(User user) {
        return orders.findByUserOrderByCreatedAtDesc(user).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> allOrders() {
        return orders.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional
    public OrderResponse checkout(User user, CheckoutRequest request) {
        List<CartItem> items = cartItems.findByUser(user);
        if (items.isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Cart is empty");
        }
        Order order = new Order();
        order.setUser(user);
        order.setShippingAddress(request.shippingAddress());
        BigDecimal total = BigDecimal.ZERO;
        for (CartItem cartItem : items) {
            Product product = cartItem.getProduct();
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(product);
            orderItem.setProductName(product.getName());
            orderItem.setUnitPrice(product.getPrice());
            orderItem.setQuantity(cartItem.getQuantity());
            order.getItems().add(orderItem);
            total = total.add(product.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity())));
        }
        order.setTotalAmount(total);
        Order saved = orders.save(order);
        cartItems.deleteByUser(user);
        return toResponse(saved);
    }

    @Transactional
    public OrderResponse updateStatus(Long id, OrderStatus status) {
        Order order = orders.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Order not found"));
        order.setStatus(status);
        return toResponse(order);
    }

    public OrderResponse toResponse(Order order) {
        User user = order.getUser();
        List<OrderItemResponse> items = order.getItems().stream()
                .map(item -> new OrderItemResponse(item.getProduct().getId(), item.getProductName(), item.getUnitPrice(), item.getQuantity(), item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity()))))
                .toList();
        return new OrderResponse(order.getId(), user.getId(), user.getName(), user.getEmail(), order.getStatus(), order.getTotalAmount(), order.getShippingAddress(), order.getCreatedAt(), items);
    }
}
