package com.example.caching.mapper;

import com.example.caching.dto.ProductRequest;
import com.example.caching.dto.ProductResponse;
import com.example.caching.entity.Product;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class ProductMapper {

    public Product toEntity(ProductRequest request) {
        return Product.builder()
                .productName(request.getProductName())
                .category(request.getCategory())
                .price(request.getPrice())
                .inStock(request.getInStock())
                .dateOfManufacture(request.getDateOfManufacture())
                .vendor(request.getVendor())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    public void updateEntity(Product product, ProductRequest request) {
        product.setProductName(request.getProductName());
        product.setCategory(request.getCategory());
        product.setPrice(request.getPrice());
        product.setInStock(request.getInStock());
        product.setDateOfManufacture(request.getDateOfManufacture());
        product.setVendor(request.getVendor());
        product.setUpdatedAt(LocalDateTime.now());
    }

    public ProductResponse toResponse(Product product) {
        return ProductResponse.builder()
                .productId(product.getProductId())
                .productName(product.getProductName())
                .category(product.getCategory())
                .price(product.getPrice())
                .inStock(product.getInStock())
                .dateOfManufacture(product.getDateOfManufacture())
                .updatedAt(product.getUpdatedAt())
                .vendor(product.getVendor())
                .build();
    }
}
