package com.example.aop.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Aspect for performance monitoring of methods annotated with @MonitorPerformance.
 * Logs warnings when method execution exceeds the configured threshold.
 * <p>
 * Can also be applied at class level to monitor all methods in the class.
 */
@Slf4j
@Aspect
@Order(3)
@Component
public class PerformanceMonitoringAspect {

    @Value("${aop.performance.slow-threshold-ms:1000}")
    private long defaultSlowThresholdMs;

    @Around("@annotation(monitorPerformance)")
    public Object monitorMethodAnnotation(ProceedingJoinPoint joinPoint, MonitorPerformance monitorPerformance) throws Throwable {
        return monitor(joinPoint, monitorPerformance);
    }

    @Around("@within(monitorPerformance)")
    public Object monitorClassAnnotation(ProceedingJoinPoint joinPoint, MonitorPerformance monitorPerformance) throws Throwable {
        return monitor(joinPoint, monitorPerformance);
    }

    private Object monitor(ProceedingJoinPoint joinPoint, MonitorPerformance monitorPerformance) throws Throwable {
        var start = System.currentTimeMillis();
        var method = joinPoint.getSignature().toShortString();

        try {
            return joinPoint.proceed();
        } finally {
            var duration = System.currentTimeMillis() - start;
            var threshold = getThreshold(monitorPerformance);

            if (duration > threshold) {
                log.warn("SLOW METHOD: {} took {}ms (threshold: {}ms)", method, duration, threshold);
            } else {
                log.debug("Method {} completed in {}ms", method, duration);
            }
        }
    }

    private long getThreshold(MonitorPerformance annotation) {
        if (annotation != null && annotation.thresholdMs() > 0) {
            return annotation.thresholdMs();
        }
        return defaultSlowThresholdMs;
    }
}
