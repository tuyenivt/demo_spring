package com.example.aop.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.UUID;

/**
 * Controller logging aspect with correlation IDs for distributed tracing.
 * Automatically intercepts all @RestController methods to provide:
 * - Unique correlation ID per request (propagated via MDC)
 * - Entry/exit logging with arguments and results
 * - Request duration tracking
 * - Exception logging with timing
 * <p>
 * This aspect is specifically for the web layer. For business logic logging,
 * use @ExecutionLogging annotation with ExecutionLoggingAspect.
 */
@Slf4j
@Aspect
@Order(1)
@Component
public class ControllerLoggingAspect {
    private static final String CORRELATION_ID_KEY = "correlationId";

    @Around("@within(org.springframework.web.bind.annotation.RestController)")
    public Object logControllerMethods(ProceedingJoinPoint joinPoint) throws Throwable {
        var correlationId = generateCorrelationId();
        var methodName = joinPoint.getSignature().toShortString();

        MDC.put(CORRELATION_ID_KEY, correlationId);
        try {
            log.info("[{}] Entering: {} with args: {}", correlationId, methodName, Arrays.toString(joinPoint.getArgs()));

            var start = System.currentTimeMillis();
            try {
                var result = joinPoint.proceed();
                var duration = System.currentTimeMillis() - start;
                log.info("[{}] Exiting: {} in {}ms with result: {}", correlationId, methodName, duration, summarizeResult(result));
                return result;
            } catch (Exception e) {
                var duration = System.currentTimeMillis() - start;
                log.error("[{}] Exception in {} after {}ms: {}", correlationId, methodName, duration, e.getMessage());
                throw e;
            }
        } finally {
            MDC.remove(CORRELATION_ID_KEY);
        }
    }

    private String generateCorrelationId() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    private String summarizeResult(Object result) {
        if (result == null) {
            return "null";
        }
        var str = result.toString();
        if (str.length() > 100) {
            return str.substring(0, 100) + "...";
        }
        return str;
    }
}
