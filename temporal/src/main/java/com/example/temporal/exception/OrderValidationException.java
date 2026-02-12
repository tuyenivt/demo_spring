package com.example.temporal.exception;

/**
 * Non-retryable exception for order validation failures.
 * <p>
 * Unlike transient failures (network timeouts, temporary service unavailability),
 * validation errors represent permanent failures caused by invalid business data.
 * Retrying these are wasteful and incorrect — the result will always be the same.
 * <p>
 * Usage in activities:
 * <pre>
 *   throw ApplicationFailure.newNonRetryableFailure(
 *       "Invalid order amount", OrderValidationException.class.getName());
 * </pre>
 * <p>
 * Or configured via RetryOptions:
 * <pre>
 *   RetryOptions.newBuilder()
 *       .setDoNotRetry(OrderValidationException.class.getName())
 *       .build()
 * </pre>
 */
public class OrderValidationException extends RuntimeException {
    public OrderValidationException(String message) {
        super(message);
    }
}
