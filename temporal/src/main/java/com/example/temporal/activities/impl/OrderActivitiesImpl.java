package com.example.temporal.activities.impl;

import com.example.temporal.activities.OrderActivities;
import com.example.temporal.exception.OrderActivitiesException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Random;

/**
 * Implementation of order activities.
 * <p>
 * ACTIVITY IMPLEMENTATION BEST PRACTICES:
 * <p>
 * 1. Use regular logging (not Workflow.getLogger())
 * - Activities are executed only once
 * - Logs are not replayed
 * <p>
 * 2. Use Activity.getExecutionContext() for activity info
 * - Get task token, workflow ID, activity ID
 * - Send heartbeats for long-running activities
 * <p>
 * 3. Handle errors appropriately
 * - Throw exceptions for retryable errors
 * - Return error status for non-retryable errors (optional)
 * <p>
 * 4. Idempotency
 * - Activities may be retried
 * - Ensure operations are idempotent or implement deduplication
 */
@Slf4j
@Component
public class OrderActivitiesImpl implements OrderActivities {
    private final Random random = new Random();

    @Override
    public void validateOrder(String orderId, long amount) {
        log.info("Validating order: orderId={}, amount={}", orderId, amount);

        // Simulate validation logic
        if (amount <= 0) {
            throw new IllegalArgumentException("Invalid amount: " + amount);
        }

        // Simulate occasional transient failure to demonstrate retry
        if (random.nextInt(10) < 2) { // 20% chance
            log.warn("Transient validation failure - will be retried");
            throw new OrderActivitiesException("Temporary validation service unavailable");
        }

        log.info("Order validation successful");
    }

    @Override
    public boolean checkInventory(String orderId, int quantity) {
        log.info("Checking inventory: orderId={}, quantity={}", orderId, quantity);

        // Simulate inventory check
        // In production: query inventory database

        // For demo: always return true
        var available = true;

        log.info("Inventory check result: available={}", available);
        return available;
    }

    @Override
    public void reserveInventory(String orderId, int quantity) {
        log.info("Reserving inventory: orderId={}, quantity={}", orderId, quantity);

        // Simulate inventory reservation
        // In production: update database with reservation

        // Simulate occasional failure
        if (random.nextInt(10) < 1) { // 10% chance
            log.warn("Transient inventory system failure - will be retried");
            throw new OrderActivitiesException("Inventory system temporarily unavailable");
        }

        log.info("Inventory reserved successfully");
    }

    @Override
    public void releaseInventory(String orderId, int quantity) {
        log.info("Releasing inventory: orderId={}, quantity={}", orderId, quantity);

        // Simulate releasing previously reserved inventory
        // In production: update database to release reservation

        // Simulate occasional failure
        if (random.nextInt(10) < 1) { // 10% chance
            log.warn("Transient inventory release failure - will be retried");
            throw new OrderActivitiesException("Inventory system temporarily unavailable");
        }

        log.info("Inventory released successfully");
    }

    @Override
    public void sendNotification(String customerId, String message) {
        log.info("Sending notification: customerId={}, message={}", customerId, message);

        // Simulate sending notification
        // In production: call email service, SMS gateway, etc.
        log.info("Notification sent successfully");
    }
}
