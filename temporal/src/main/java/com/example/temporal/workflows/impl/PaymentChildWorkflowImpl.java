package com.example.temporal.workflows.impl;

import com.example.temporal.activities.PaymentActivities;
import com.example.temporal.workflows.PaymentChildWorkflow;
import io.temporal.activity.ActivityOptions;
import io.temporal.common.RetryOptions;
import io.temporal.workflow.Workflow;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;

/**
 * Implementation of payment processing child workflow.
 * <p>
 * This workflow demonstrates a multistep payment process:
 * 1. Authorize payment (reserve funds)
 * 2. Capture payment (actually charge)
 * <p>
 * Each step is an activity with its own retry policy.
 */
@Slf4j
public class PaymentChildWorkflowImpl implements PaymentChildWorkflow {

    /**
     * Activity options for payment activities.
     * <p>
     * Payment operations are critical and may face transient failures:
     * - Network timeouts
     * - Payment gateway temporary unavailability
     * - Rate limiting
     * <p>
     * Retry policy handles these gracefully.
     */
    private final ActivityOptions paymentActivityOptions = ActivityOptions.newBuilder()
            .setStartToCloseTimeout(Duration.ofSeconds(30))
            .setRetryOptions(RetryOptions.newBuilder()
                    .setInitialInterval(Duration.ofSeconds(2))
                    .setBackoffCoefficient(2.0)
                    .setMaximumInterval(Duration.ofSeconds(20))
                    .setMaximumAttempts(5) // More retries for critical payment operations
                    .build())
            .build();

    private final PaymentActivities activities = Workflow.newActivityStub(PaymentActivities.class, paymentActivityOptions);

    @Override
    public String processPayment(String orderId, String customerId, long amount) {
        log.info("Payment child workflow started for orderId={}", orderId);

        try {
            // Step 1: Authorize payment (reserve funds)
            log.info("Authorizing payment for amount={}", amount);
            String authId = activities.authorizePayment(customerId, amount);
            log.info("Payment authorized: authId={}", authId);

            // Step 2: Capture payment (actually charge)
            // In real-world scenarios, there might be a delay or business logic here
            log.info("Capturing payment with authId={}", authId);
            activities.capturePayment(authId, amount);
            log.info("Payment captured successfully");

            return "Payment successful: " + authId;
        } catch (Exception e) {
            log.error("Payment failed for orderId={}: {}", orderId, e.getMessage());

            // In production, implement compensation logic:
            // - Void authorization if capture fails
            // - Update order status
            // - Send alerts
            throw e; // Propagate to parent workflow
        }
    }
}
