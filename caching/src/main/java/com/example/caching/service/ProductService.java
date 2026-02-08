package com.example.caching.service;

import com.example.caching.entity.Product;
import com.example.caching.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "product", unless = "#result == null", key = "#id")
    public Optional<Product> findById(Long id) {
        try {
            return productRepository.findById(id);
        } catch (DataAccessException ex) {
            log.warn("Can't get Product from cache with id = {}, got from database", id);
            return productRepository.findById(id);
        }
    }

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "product_list", unless = "#result == null", key = "'myPrefix_'.concat(#productName)")
    public List<Product> findByProductNameOrderByUpdatedAtDesc(String productName) {
        try {
            return productRepository.findByProductName(productName, Sort.by("updatedAt").descending());
        } catch (DataAccessException ex) {
            log.warn("Can't get Products from cache with name contain {}, got from database", productName);
            return productRepository.findByProductName(productName, Sort.by("updatedAt").descending());
        }
    }

    @Transactional
    @Caching(
            put = @CachePut(cacheNames = "product", condition = "#result.inStock gt 0", key = "#result.productId"),
            evict = {
                    @CacheEvict(cacheNames = "product_list", allEntries = true),
                    @CacheEvict(cacheNames = "product", condition = "#result.inStock eq 0", key = "#result.productId")
            }
    )
    public Product save(Product product) {
        return productRepository.save(product);
    }
}
