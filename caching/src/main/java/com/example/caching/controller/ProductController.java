package com.example.caching.controller;

import com.example.caching.dto.ProductRequest;
import com.example.caching.dto.ProductResponse;
import com.example.caching.mapper.ProductMapper;
import com.example.caching.service.ProductService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Validated
public class ProductController {

    private final ProductService productService;
    private final ProductMapper productMapper;

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getById(@PathVariable Long id) {
        return productService.findById(id)
                .map(productMapper::toResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/search")
    public List<ProductResponse> searchByName(@RequestParam @NotBlank(message = "Name parameter is required") String name) {
        return productService.findByProductNameOrderByUpdatedAtDesc(name).stream()
                .map(productMapper::toResponse)
                .toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductResponse create(@Valid @RequestBody ProductRequest request) {
        var product = productMapper.toEntity(request);
        var savedProduct = productService.save(product);
        return productMapper.toResponse(savedProduct);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductResponse> update(@PathVariable Long id, @Valid @RequestBody ProductRequest request) {
        return productService.findById(id)
                .map(existingProduct -> {
                    productMapper.updateEntity(existingProduct, request);
                    var updatedProduct = productService.save(existingProduct);
                    return ResponseEntity.ok(productMapper.toResponse(updatedProduct));
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
