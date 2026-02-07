package com.example.aop.aspect.retry;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Aspect that retries method execution on specified exceptions.
 */
@Slf4j
@Aspect
@Order(0)
@Component
public class RetryAspect {

    @Around("@annotation(retryable)")
    public Object retry(ProceedingJoinPoint joinPoint, Retryable retryable) throws Throwable {
        var method = joinPoint.getSignature().toShortString();
        int maxAttempts = retryable.maxAttempts();
        Throwable lastException = null;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                if (attempt > 1) {
                    log.info("Retry attempt {}/{} for {}", attempt, maxAttempts, method);
                }
                return joinPoint.proceed();
            } catch (Throwable e) {
                if (!isRetryable(e, retryable.retryOn())) {
                    throw e;
                }
                lastException = e;
                log.warn("Attempt {}/{} failed for {}: {}", attempt, maxAttempts, method, e.getMessage());
            }
        }

        log.error("All {} attempts exhausted for {}", maxAttempts, method);
        throw lastException;
    }

    private boolean isRetryable(Throwable thrown, Class<? extends Throwable>[] retryOn) {
        for (var exceptionClass : retryOn) {
            if (exceptionClass.isInstance(thrown)) {
                return true;
            }
        }
        return false;
    }
}
