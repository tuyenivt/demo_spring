package com.example.caching.service;

import com.example.caching.entity.Product;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ProductService {

    Optional<Product> findById(Long id);

    Product findFirstByProductIdAndDateOfManufactureOrderByDateOfManufactureDesc(Long productId, LocalDateTime dateOfManufacture);

    List<Product> findProductNameInOrderByUpdatedAtDesc(String productName);
}
