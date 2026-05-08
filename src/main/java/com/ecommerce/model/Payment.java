package com.ecommerce.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "payments")
public class Payment {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @OneToOne(fetch = FetchType.LAZY, optional = false) private Order order;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private PaymentProvider provider;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private PaymentStatus status = PaymentStatus.CREATED;
    @Column(nullable = false, precision = 12, scale = 2) private BigDecimal amount;
    @Column(nullable = false) private String transactionId;
    @Column(nullable = false) private Instant createdAt = Instant.now();
    public Long getId(){return id;} public void setId(Long id){this.id=id;}
    public Order getOrder(){return order;} public void setOrder(Order order){this.order=order;}
    public PaymentProvider getProvider(){return provider;} public void setProvider(PaymentProvider provider){this.provider=provider;}
    public PaymentStatus getStatus(){return status;} public void setStatus(PaymentStatus status){this.status=status;}
    public BigDecimal getAmount(){return amount;} public void setAmount(BigDecimal amount){this.amount=amount;}
    public String getTransactionId(){return transactionId;} public void setTransactionId(String transactionId){this.transactionId=transactionId;}
    public Instant getCreatedAt(){return createdAt;} public void setCreatedAt(Instant createdAt){this.createdAt=createdAt;}
}