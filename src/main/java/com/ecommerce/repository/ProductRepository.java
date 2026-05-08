package com.ecommerce.repository;
import com.ecommerce.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
public interface ProductRepository extends JpaRepository<Product, Long> { List<Product> findByActiveTrue(); List<Product> findByActiveTrueAndCategorySlug(String slug); List<Product> findByNameContainingIgnoreCaseAndActiveTrue(String keyword); Optional<Product> findBySku(String sku); boolean existsBySku(String sku); }