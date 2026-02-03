package com.example.caching.config;

import com.example.caching.repository.ProductCacheRepository;
import com.example.caching.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CacheWarmer implements ApplicationRunner {

    private final ProductRepository productRepository;
    private final ProductCacheRepository productCacheRepository;

    @Override
    public void run(ApplicationArguments args) {
        log.info("Warming product cache...");
        productRepository.findAll().forEach(product ->
                productCacheRepository.findById(product.getProductId())
        );
        log.info("Cache warming completed");
    }
}
