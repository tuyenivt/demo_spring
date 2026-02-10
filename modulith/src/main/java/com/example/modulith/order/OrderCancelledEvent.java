package com.example.modulith.order;

import java.time.Instant;

public record OrderCancelledEvent(
        Long orderId,
        String sku,
        Integer quantity,
        Instant occurredAt
) {
}
