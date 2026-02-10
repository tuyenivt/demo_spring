package com.example.modulith.order;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.Instant;

@Builder
public record OrderResponse(
        Long id,
        Long customerId,
        String status,
        BigDecimal totalAmount,
        String sku,
        Integer quantity,
        Instant createdAt
) {
}
