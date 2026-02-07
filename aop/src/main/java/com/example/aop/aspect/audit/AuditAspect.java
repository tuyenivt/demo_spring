package com.example.aop.aspect.audit;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Arrays;

/**
 * Aspect for audit trail logging.
 * Tracks data modifications on methods annotated with @Audited.
 */
@Slf4j
@Aspect
@Component
public class AuditAspect {

    @AfterReturning(pointcut = "@annotation(audited)", returning = "result")
    public void auditSuccess(JoinPoint joinPoint, Audited audited, Object result) {
        var auditLog = AuditLog.builder()
                .timestamp(Instant.now())
                .user(getCurrentUser())
                .action(audited.action())
                .entity(audited.entity())
                .method(joinPoint.getSignature().toShortString())
                .args(Arrays.toString(joinPoint.getArgs()))
                .result(result != null ? summarizeResult(result) : "null")
                .build();

        log.info("AUDIT: {}", auditLog);
    }

    @AfterThrowing(pointcut = "@annotation(audited)", throwing = "exception")
    public void auditFailure(JoinPoint joinPoint, Audited audited, Throwable exception) {
        var auditLog = AuditLog.builder()
                .timestamp(Instant.now())
                .user(getCurrentUser())
                .action(audited.action() + "_FAILED")
                .entity(audited.entity())
                .method(joinPoint.getSignature().toShortString())
                .args(Arrays.toString(joinPoint.getArgs()))
                .result("EXCEPTION: " + exception.getMessage())
                .build();

        log.warn("AUDIT: {}", auditLog);
    }

    private String getCurrentUser() {
        // In a real application, this would get the user from SecurityContextHolder:
        // Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        // return auth != null ? auth.getName() : "anonymous";
        return "anonymous";
    }

    private String summarizeResult(Object result) {
        var str = result.toString();
        if (str.length() > 200) {
            return str.substring(0, 200) + "...";
        }
        return str;
    }
}
