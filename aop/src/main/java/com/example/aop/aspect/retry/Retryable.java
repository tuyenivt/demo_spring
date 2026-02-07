package com.example.aop.aspect.retry;

import java.lang.annotation.*;

/**
 * Annotation to mark methods for automatic retry on failure.
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Retryable {

    /**
     * Maximum number of attempts (including the initial call).
     */
    int maxAttempts() default 3;

    /**
     * Exception types that trigger a retry.
     */
    Class<? extends Throwable>[] retryOn() default {RuntimeException.class};
}
