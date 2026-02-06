package com.example.temporal.activities.impl;

import com.example.temporal.activities.PaymentActivities;
import com.example.temporal.exception.PaymentActivitiesException;
import io.temporal.activity.Activity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Random;
import java.util.UUID;

/**
 * Implementation of payment activities.
 * <p>
 * Simulates integration with a payment gateway.
 * <p>
 * ACTIVITY HEARTBEAT:
 * - Used for long-running activities to report progress
 * - Prevents Temporal from considering activity as failed
 * - Enables cancellation detection during long operations
 * - Heartbeat details can be used for progress tracking
 */
@Slf4j
@Component
public class PaymentActivitiesImpl implements PaymentActivities {
    private final Random random = new Random();

    @Override
    public String authorizePayment(String customerId, long amount) {
        log.info("Authorizing payment: customerId={}, amount={}", customerId, amount);

        // Report heartbeat - connecting to payment gateway
        Activity.getExecutionContext().heartbeat("Connecting to payment gateway");

        // Simulate connection delay
        simulateWork(500);

        // Report heartbeat - processing authorization
        Activity.getExecutionContext().heartbeat("Processing authorization");

        // Simulate processing delay
        simulateWork(500);

        // Simulate occasional transient failure
        if (random.nextInt(10) < 2) { // 20% chance
            log.warn("Payment gateway timeout - will be retried");
            throw new PaymentActivitiesException("Payment gateway timeout");
        }

        // Report heartbeat - finalizing
        Activity.getExecutionContext().heartbeat("Finalizing authorization");

        var authId = "AUTH-" + UUID.randomUUID().toString().substring(0, 8);
        log.info("Payment authorized: authId={}", authId);
        return authId;
    }

    @Override
    public void capturePayment(String authorizationId, long amount) {
        log.info("Capturing payment: authId={}, amount={}", authorizationId, amount);

        // Report heartbeat - starting capture
        Activity.getExecutionContext().heartbeat("Starting payment capture");

        // Simulate payment capture
        simulateWork(300);

        // Simulate occasional failure
        if (random.nextInt(10) < 1) { // 10% chance
            log.warn("Payment capture failed - will be retried");
            throw new PaymentActivitiesException("Payment capture failed");
        }

        // Report heartbeat - completed
        Activity.getExecutionContext().heartbeat("Payment capture completed");

        log.info("Payment captured successfully");
    }

    @Override
    public void refundPayment(String authorizationId, long amount) {
        log.info("Refunding payment: authId={}, amount={}", authorizationId, amount);

        // Report heartbeat - initiating refund
        Activity.getExecutionContext().heartbeat("Initiating refund");

        // Simulate refund processing
        simulateWork(500);

        // Simulate occasional failure
        if (random.nextInt(10) < 1) { // 10% chance
            log.warn("Payment refund failed - will be retried");
            throw new PaymentActivitiesException("Payment refund failed");
        }

        // Report heartbeat - completed
        Activity.getExecutionContext().heartbeat("Refund completed");

        log.info("Payment refunded successfully");
    }

    /**
     * Simulate work with cancellation check.
     */
    private void simulateWork(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new PaymentActivitiesException("Activity interrupted");
        }
    }
}
