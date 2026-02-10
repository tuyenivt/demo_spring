package com.example.modulith.inventory;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record ProductResponse(
        Long id,
        String sku,
        String name,
        BigDecimal price,
        Integer stockQuantity
) {
}
