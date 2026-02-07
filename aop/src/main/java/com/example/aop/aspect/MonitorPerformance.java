package com.example.aop.aspect;

import java.lang.annotation.*;

/**
 * Annotation to enable performance monitoring on methods.
 * Logs warnings when execution time exceeds the configured threshold.
 * <p>
 * Configure threshold via application.properties:
 * aop.performance.slow-threshold-ms=1000
 */
@Documented
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface MonitorPerformance {
    /**
     * Optional custom threshold in milliseconds. If not specified, uses global config.
     */
    long thresholdMs() default -1;
}
