package com.example.modulith.order;

import java.math.BigDecimal;
import java.time.Instant;

public record OrderCreatedEvent(
        Long orderId,
        Long customerId,
        BigDecimal totalAmount,
        String sku,
        Integer quantity,
        Instant createdAt
) {
}
