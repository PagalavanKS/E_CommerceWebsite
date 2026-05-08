package com.ecommerce.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "inventory_transactions")
public class InventoryTransaction {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) private Product product;
    @Column(nullable = false) private Integer quantityChange;
    @Column(nullable = false) private String reason;
    @Column(nullable = false) private Instant createdAt = Instant.now();
    public Long getId(){return id;} public void setId(Long id){this.id=id;}
    public Product getProduct(){return product;} public void setProduct(Product product){this.product=product;}
    public Integer getQuantityChange(){return quantityChange;} public void setQuantityChange(Integer quantityChange){this.quantityChange=quantityChange;}
    public String getReason(){return reason;} public void setReason(String reason){this.reason=reason;}
    public Instant getCreatedAt(){return createdAt;} public void setCreatedAt(Instant createdAt){this.createdAt=createdAt;}
}