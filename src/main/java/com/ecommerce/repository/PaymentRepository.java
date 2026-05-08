package com.ecommerce.repository;
import com.ecommerce.model.Order;
import com.ecommerce.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
public interface PaymentRepository extends JpaRepository<Payment, Long> { Optional<Payment> findByOrder(Order order); }