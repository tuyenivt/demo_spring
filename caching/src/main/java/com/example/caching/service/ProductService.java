package com.example.caching.service;

import com.example.caching.entity.Product;
import com.example.caching.repository.ProductCacheRepository;
import com.example.caching.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductCacheRepository productCacheRepository;
    private final ProductRepository productRepository;

    public Optional<Product> findById(Long id) {
        try {
            return productCacheRepository.findById(id);
        } catch (Exception ex) {
            log.warn("Can't get Product from cache with id = {}, got from database", id);
            return productRepository.findById(id);
        }
    }

    public List<Product> findByProductNameOrderByUpdatedAtDesc(String productName) {
        try {
            return productCacheRepository.findByProductName(productName, Sort.by("updatedAt").descending());
        } catch (Exception ex) {
            log.warn("Can't get Products from cache with name contain {}, got from database", productName);
            return productRepository.findByProductName(productName, Sort.by("updatedAt").descending());
        }
    }
}
