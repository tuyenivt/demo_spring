package com.example.caching.service.impl;

import com.example.caching.entity.Product;
import com.example.caching.repository.ProductCacheRepository;
import com.example.caching.repository.ProductRepository;
import com.example.caching.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductCacheRepository productCacheRepository;
    private final ProductRepository productRepository;

    @Override
    public Optional<Product> findById(Long id) {
        try {
            return productCacheRepository.findById(id);
        } catch (Exception ex) {
            log.warn("Can't get Product from cache with id = {}, got from database", id);
            return productRepository.findById(id);
        }
    }

    @Override
    public Product findFirstByProductIdAndDateOfManufactureOrderByDateOfManufactureDesc(Long productId, LocalDateTime dateOfManufacture) {
        try {
            return productCacheRepository.findFirstByProductIdAndDateOfManufactureOrderByDateOfManufactureDesc(productId, dateOfManufacture);
        } catch (Exception ex) {
            log.warn("Can't get Product from cache with id = {}, dateOfManufacture = {}, got from database", productId, dateOfManufacture);
            return productRepository.findFirstByProductIdAndDateOfManufactureOrderByDateOfManufactureDesc(productId, dateOfManufacture);
        }
    }

    @Override
    public List<Product> findProductNameInOrderByUpdatedAtDesc(String productName) {
        try {
            return productCacheRepository.findProductNameInOrderByUpdatedAtDesc(productName);
        } catch (Exception ex) {
            log.warn("Can't get Products from cache with name contain {}, got from database", productName);
            return productRepository.findProductNameInOrderByUpdatedAtDesc(productName);
        }
    }
}
