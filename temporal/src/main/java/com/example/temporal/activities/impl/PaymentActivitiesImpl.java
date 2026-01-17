package com.example.temporal.activities.impl;

import com.example.temporal.activities.PaymentActivities;
import com.example.temporal.exception.PaymentActivitiesException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Random;
import java.util.UUID;

/**
 * Implementation of payment activities.
 * <p>
 * Simulates integration with a payment gateway.
 */
@Slf4j
@Component
public class PaymentActivitiesImpl implements PaymentActivities {
    private final Random random = new Random();

    @Override
    public String authorizePayment(String customerId, long amount) {
        log.info("Authorizing payment: customerId={}, amount={}", customerId, amount);

        // Simulate payment gateway call
        // In production: call Stripe, PayPal, etc.

        // Simulate occasional transient failure
        if (random.nextInt(10) < 2) { // 20% chance
            log.warn("Payment gateway timeout - will be retried");
            throw new PaymentActivitiesException("Payment gateway timeout");
        }

        var authId = "AUTH-" + UUID.randomUUID().toString().substring(0, 8);
        log.info("Payment authorized: authId={}", authId);
        return authId;
    }

    @Override
    public void capturePayment(String authorizationId, long amount) {
        log.info("Capturing payment: authId={}, amount={}", authorizationId, amount);

        // Simulate payment capture
        // In production: capture the authorized payment

        // Simulate occasional failure
        if (random.nextInt(10) < 1) { // 10% chance
            log.warn("Payment capture failed - will be retried");
            throw new PaymentActivitiesException("Payment capture failed");
        }

        log.info("Payment captured successfully");
    }
}
