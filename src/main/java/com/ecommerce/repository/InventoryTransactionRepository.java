package com.ecommerce.repository;
import com.ecommerce.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface InventoryTransactionRepository extends JpaRepository<InventoryTransaction, Long> { List<InventoryTransaction> findByProductOrderByCreatedAtDesc(Product product); }