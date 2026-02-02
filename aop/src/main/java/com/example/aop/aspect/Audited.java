package com.example.aop.aspect;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark methods for audit trail logging.
 * Use this to track data modifications for compliance purposes.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Audited {

    /**
     * The action being performed (e.g., "CREATE", "UPDATE", "DELETE").
     */
    String action();

    /**
     * The entity type being affected (e.g., "Account", "User", "Order").
     */
    String entity();
}
