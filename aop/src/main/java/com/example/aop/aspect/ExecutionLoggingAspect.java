package com.example.aop.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * Aspect for logging method execution details when annotated with @ExecutionLogging.
 * Provides detailed logging of method signature, arguments, execution time, and return values.
 * <p>
 * Use this aspect to opt-in specific methods for detailed execution tracking.
 */
@Slf4j
@Aspect
@Order(2)
@Component
public class ExecutionLoggingAspect {

    @Around("@annotation(ExecutionLogging)")
    public Object logMethodExecution(ProceedingJoinPoint joinPoint) throws Throwable {
        var signature = joinPoint.getSignature();
        var args = joinPoint.getArgs();

        log.info("Executing: {} with args: {}", signature.toShortString(), Arrays.toString(args));

        var start = System.currentTimeMillis();
        try {
            var result = joinPoint.proceed();
            var duration = System.currentTimeMillis() - start;

            log.info("Completed: {} in {}ms with result: {}",
                    signature.toShortString(), duration, summarizeResult(result));

            return result;
        } catch (Throwable e) {
            var duration = System.currentTimeMillis() - start;
            log.error("Failed: {} after {}ms with exception: {}",
                    signature.toShortString(), duration, e.getMessage());
            throw e;
        }
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
