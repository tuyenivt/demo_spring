package com.example.modulith.inventory;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record ReserveStockCommand(
        @NotBlank(message = "SKU is required")
        String sku,

        @Positive(message = "Quantity must be positive")
        Integer quantity
) {
}
