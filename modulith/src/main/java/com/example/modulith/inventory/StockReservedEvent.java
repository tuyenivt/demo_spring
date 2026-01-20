package com.example.modulith.inventory;

public record StockReservedEvent(
        String sku,
        Integer quantity,
        Long productId
) {
}
