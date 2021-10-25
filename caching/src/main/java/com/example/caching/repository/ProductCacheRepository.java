package com.example.caching.repository;

import com.example.caching.entity.Product;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ProductCacheRepository extends JpaRepository<Product, Long> {

    @Caching(put = {
            @CachePut(cacheNames = "product", condition = "#entity.inStock gt 0", key = "#result.productId + '_' + #result.dateOfManufacture")
    }, evict = {
            @CacheEvict(cacheNames = "product_list", key = "#result.productName + T(com.example.caching.enums.Category).PRODUCT"),
            @CacheEvict(cacheNames = "product", condition = "#entity.inStock eq 0", key = "#result.productId + #result.dateOfManufacture")
    })
    @Override
    <S extends Product> S save(S entity);

    @Cacheable(cacheNames = "product", unless = "#result == null", key = "#productId")
    @Override
    Optional<Product> findById(Long productId);

    @Cacheable(cacheNames = "product", unless = "#result == null", key = "#productId + '_' + #dateOfManufacture")
    Product findFirstByProductIdAndDateOfManufactureOrderByDateOfManufactureDesc(Long productId, LocalDateTime dateOfManufacture);

    @Cacheable(cacheNames = "product_list", unless = "#result == null", key = "'myPrefix_'.concat(#productName)")
    List<Product> findProductNameInOrderByUpdatedAtDesc(String productName);
}
