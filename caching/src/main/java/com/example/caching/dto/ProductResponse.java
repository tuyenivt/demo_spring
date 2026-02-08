package com.example.caching.dto;

import com.example.caching.enums.Category;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductResponse {

    private Long productId;
    private String productName;
    private Category category;
    private BigDecimal price;
    private Long inStock;
    private LocalDateTime dateOfManufacture;
    private LocalDateTime updatedAt;
    private String vendor;
}
