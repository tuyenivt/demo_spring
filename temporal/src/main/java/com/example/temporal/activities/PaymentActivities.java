package com.example.temporal.activities;

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;

/**
 * Activity interface for payment operations.
 */
@ActivityInterface
public interface PaymentActivities {

    /**
     * Authorizes a payment (reserves funds).
     *
     * @return Authorization ID
     */
    @ActivityMethod
    String authorizePayment(String customerId, long amount);

    /**
     * Captures an authorized payment (actually charges).
     */
    @ActivityMethod
    void capturePayment(String authorizationId, long amount);

    /**
     * Refunds a captured payment (saga compensation).
     */
    @ActivityMethod
    void refundPayment(String authorizationId, long amount);
}
