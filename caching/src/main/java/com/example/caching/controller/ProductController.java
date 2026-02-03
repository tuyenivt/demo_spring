package com.example.caching.controller;

import com.example.caching.entity.Product;
import com.example.caching.repository.ProductCacheRepository;
import com.example.caching.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final ProductCacheRepository productCacheRepository;

    @GetMapping("/{id}")
    public ResponseEntity<Product> getById(@PathVariable Long id) {
        return productService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/search")
    public List<Product> searchByName(@RequestParam String name) {
        return productService.findByProductNameOrderByUpdatedAtDesc(name);
    }

    @PostMapping
    public Product create(@RequestBody Product product) {
        return productCacheRepository.save(product);
    }

    @PutMapping("/{id}")
    public Product update(@PathVariable Long id, @RequestBody Product product) {
        product.setProductId(id);
        return productCacheRepository.save(product);
    }
}
