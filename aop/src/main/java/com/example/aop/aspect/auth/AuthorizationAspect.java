package com.example.aop.aspect.auth;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

/**
 * Aspect that enforces role-based authorization using @RequiresRole.
 */
@Slf4j
@Aspect
@Component
public class AuthorizationAspect {

    @Before("@annotation(requiresRole)")
    public void checkRole(JoinPoint joinPoint, RequiresRole requiresRole) {
        var requiredRole = requiresRole.value();
        var currentRole = SecurityContext.getRole();
        var method = joinPoint.getSignature().toShortString();

        if (!requiredRole.equals(currentRole)) {
            log.warn("ACCESS DENIED: {} requires role '{}' but current role is '{}'", method, requiredRole, currentRole);
            throw new AccessDeniedException(
                    "Access denied: required role '" + requiredRole + "' but was '" + currentRole + "'"
            );
        }

        log.info("ACCESS GRANTED: {} for role '{}'", method, currentRole);
    }
}
