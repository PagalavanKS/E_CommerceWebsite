package com.ecommerce.config;

import com.ecommerce.model.Category;
import com.ecommerce.model.Product;
import com.ecommerce.model.Role;
import com.ecommerce.model.User;
import com.ecommerce.repository.CategoryRepository;
import com.ecommerce.repository.ProductRepository;
import com.ecommerce.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;

@Configuration
public class DataSeeder {
    @Bean
    CommandLineRunner seedData(UserRepository users, CategoryRepository categories, ProductRepository products, PasswordEncoder encoder) {
        return args -> {
            if (!users.existsByEmail("admin@shop.com")) {
                User admin = new User();
                admin.setName("Admin");
                admin.setEmail("admin@shop.com");
                admin.setPassword(encoder.encode("admin123"));
                admin.setRole(Role.ADMIN);
                users.save(admin);
            }
            if (!users.existsByEmail("user@shop.com")) {
                User user = new User();
                user.setName("Demo User");
                user.setEmail("user@shop.com");
                user.setPassword(encoder.encode("user123"));
                user.setRole(Role.USER);
                users.save(user);
            }
            Category electronics = categories.findBySlug("electronics").orElseGet(() -> {
                Category category = new Category();
                category.setName("Electronics");
                category.setSlug("electronics");
                category.setDescription("Phones, audio, smart devices, and accessories");
                return categories.save(category);
            });
            Category fashion = categories.findBySlug("fashion").orElseGet(() -> {
                Category category = new Category();
                category.setName("Fashion");
                category.setSlug("fashion");
                category.setDescription("Everyday style and essentials");
                return categories.save(category);
            });
            create(products, electronics, "Wireless Headphones", "AUD-1001", "Noise cancelling over-ear headphones", new BigDecimal("4999.00"), 25);
            create(products, electronics, "Smart Watch", "WCH-2201", "Fitness tracking smart watch", new BigDecimal("8999.00"), 18);
            create(products, fashion, "Minimal Backpack", "BAG-4100", "Water-resistant daily backpack", new BigDecimal("2499.00"), 40);
        };
    }

    private void create(ProductRepository products, Category category, String name, String sku, String description, BigDecimal price, int stock) {
        if (products.existsBySku(sku)) {
            return;
        }
        Product product = new Product();
        product.setName(name);
        product.setSku(sku);
        product.setDescription(description);
        product.setPrice(price);
        product.setStockQuantity(stock);
        product.setCategory(category);
        products.save(product);
    }
}