package com.example.modulith.customer;

import lombok.Builder;

import java.time.Instant;

/**
 * Public API response for customer operations.
 */
@Builder
public record CustomerResponse(
        Long id,
        String name,
        String email,
        Instant registeredAt
) {
}
