package com.ecommerce;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@EnableCaching
@SpringBootApplication
public class ECommerceBackendApplication {
    public static void main(String[] args) {
        SpringApplication.run(ECommerceBackendApplication.class, args);
    }
}