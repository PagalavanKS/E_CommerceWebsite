# E-Commerce Backend System with Spring Boot

Advanced REST backend for an e-commerce platform with JWT authentication, Spring Security authorization, product catalog, categories, cart and order management, mock/Razorpay-style payments, admin APIs, inventory management, JPA persistence, and cache support.

## Demo Accounts

Admin: admin@shop.com / admin123
User: user@shop.com / user123

## Run Locally

```bash
cd D:\E-Commerce_withSB
mvn spring-boot:run
```

Frontend app: `http://localhost:8080/`

Health check: `GET http://localhost:8080/api/health`

H2 console: `http://localhost:8080/h2-console`

JDBC URL: `jdbc:h2:mem:ecommerce`, user `sa`, empty password.

## Profiles

PostgreSQL: `mvn spring-boot:run -Dspring-boot.run.profiles=postgres`

MySQL: `mvn spring-boot:run -Dspring-boot.run.profiles=mysql`

Redis caching: `mvn spring-boot:run -Dspring-boot.run.profiles=redis`

Profiles can be combined, for example `postgres,redis`.

## API Overview

Auth: `POST /api/auth/register`, `POST /api/auth/login`

Public catalog: `GET /api/categories`, `GET /api/products`, `GET /api/products?category=electronics`, `GET /api/products/search?q=watch`, `GET /api/products/{id}`

Cart: `GET /api/cart`, `POST /api/cart/items`, `PUT /api/cart/items/{itemId}`, `DELETE /api/cart/items/{itemId}`

Orders: `POST /api/orders/checkout`, `GET /api/orders`

Payments: `POST /api/payments` with `MOCK` or `RAZORPAY`

Admin: `GET /api/admin/dashboard`, `POST /api/admin/categories`, `POST /api/admin/products`, `PUT /api/admin/products/{id}`, `PATCH /api/admin/products/{id}/inventory`, `GET /api/admin/orders`, `PATCH /api/admin/orders/{id}/status`

Use `Authorization: Bearer <token>` for protected endpoints. The frontend handles this automatically after login.

## Build

```bash
mvn test
mvn package
```

## Docker

```bash
docker build -t ecommerce-backend .
docker run -p 8080:8080 ecommerce-backend
```
