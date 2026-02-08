package com.example.caching.config;

import com.example.caching.repository.ProductRepository;
import com.example.caching.service.ProductService;
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
    private final ProductService productService;

    @Override
    public void run(ApplicationArguments args) {
        log.info("Warming product cache...");
        try {
            productRepository.findAll().forEach(product -> productService.findById(product.getProductId()));
            log.info("Cache warming completed");
        } catch (Exception ex) {
            log.warn("Cache warming failed - cache will be populated lazily on first request: {}", ex.getMessage());
        }
    }
}
