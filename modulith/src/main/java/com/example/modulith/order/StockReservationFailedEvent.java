package com.example.modulith.order;

import java.time.Instant;

public record StockReservationFailedEvent(
        Long orderId,
        String sku,
        Integer quantity,
        String reason,
        Instant occurredAt
) {
}
