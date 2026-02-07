package com.example.aop.aspect.auth;

/**
 * Thrown when a method annotated with @RequiresRole is called without
 * the required role set in the SecurityContext.
 */
public class AccessDeniedException extends RuntimeException {
    public AccessDeniedException(String message) {
        super(message);
    }
}
