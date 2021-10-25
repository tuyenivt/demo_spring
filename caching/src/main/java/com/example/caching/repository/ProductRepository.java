package com.example.caching.repository;

import com.example.caching.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {

    Product findFirstByProductIdAndDateOfManufactureOrderByDateOfManufactureDesc(Long productId, LocalDateTime dateOfManufacture);

    List<Product> findProductNameInOrderByUpdatedAtDesc(String productName);
}
