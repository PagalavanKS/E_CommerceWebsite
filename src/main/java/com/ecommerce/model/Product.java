package com.ecommerce.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "products", indexes = {@Index(name = "idx_products_name", columnList = "name"), @Index(name = "idx_products_active", columnList = "active")})
public class Product {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false) private String name;
    @Column(nullable = false, unique = true) private String sku;
    @Column(length = 2000) private String description;
    @Column(length = 2000) private String imageUrl;
    @Column(nullable = false, precision = 12, scale = 2) private BigDecimal price;
    @Column(nullable = false) private Integer stockQuantity = 0;
    @Column(nullable = false) private boolean active = true;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) private Category category;
    @Column(nullable = false) private Instant createdAt = Instant.now();
    public Long getId(){return id;} public void setId(Long id){this.id=id;}
    public String getName(){return name;} public void setName(String name){this.name=name;}
    public String getSku(){return sku;} public void setSku(String sku){this.sku=sku;}
    public String getDescription(){return description;} public void setDescription(String description){this.description=description;}
    public String getImageUrl(){return imageUrl;} public void setImageUrl(String imageUrl){this.imageUrl=imageUrl;}
    public BigDecimal getPrice(){return price;} public void setPrice(BigDecimal price){this.price=price;}
    public Integer getStockQuantity(){return stockQuantity;} public void setStockQuantity(Integer stockQuantity){this.stockQuantity=stockQuantity;}
    public boolean isActive(){return active;} public void setActive(boolean active){this.active=active;}
    public Category getCategory(){return category;} public void setCategory(Category category){this.category=category;}
    public Instant getCreatedAt(){return createdAt;} public void setCreatedAt(Instant createdAt){this.createdAt=createdAt;}
}
