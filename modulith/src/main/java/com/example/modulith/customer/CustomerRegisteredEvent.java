package com.example.modulith.customer;

import java.time.Instant;

/**
 * Public API event published when a customer is registered.
 * This is part of the module's public interface.
 */
public record CustomerRegisteredEvent(
        Long customerId,
        String name,
        String email,
        Instant registeredAt
) {
}
