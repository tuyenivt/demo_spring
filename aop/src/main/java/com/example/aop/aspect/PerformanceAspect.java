package com.example.aop.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

/**
 * Aspect for performance monitoring of service layer methods.
 * Logs warnings for slow method executions.
 */
@Slf4j
@Aspect
@Component
public class PerformanceAspect {
    private static final long SLOW_THRESHOLD_MS = 1000;

    @Around("@within(org.springframework.stereotype.Service)")
    public Object monitorPerformance(ProceedingJoinPoint joinPoint) throws Throwable {
        var start = System.currentTimeMillis();
        var method = joinPoint.getSignature().toShortString();

        try {
            return joinPoint.proceed();
        } finally {
            var duration = System.currentTimeMillis() - start;

            if (duration > SLOW_THRESHOLD_MS) {
                log.warn("SLOW METHOD: {} took {}ms (threshold: {}ms)", method, duration, SLOW_THRESHOLD_MS);
            } else {
                log.debug("Method {} completed in {}ms", method, duration);
            }

            // In a real application, record metrics to Micrometer:
            // meterRegistry.timer("method.execution", "method", method).record(duration, TimeUnit.MILLISECONDS);
        }
    }
}
