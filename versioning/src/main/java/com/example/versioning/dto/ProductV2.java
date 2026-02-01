package com.example.versioning.dto;

/**
 * V2 product - extended with description and SKU.
 */
public record ProductV2(
        String name,
        double price,
        String description,
        String sku
) {
}
