package com.example.temporal.dto;

/**
 * Request DTO for creating a new order.
 */
public record OrderRequest(
        String customerId,
        long amount,
        String description
) {
    public OrderRequest {
        if (customerId == null || customerId.isBlank()) {
            throw new IllegalArgumentException("customerId is required");
        }
        if (amount <= 0) {
            throw new IllegalArgumentException("amount must be positive");
        }
    }
}
