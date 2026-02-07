package com.example.aop.aspect.audit;

import java.lang.annotation.*;

/**
 * Annotation to mark methods for audit trail logging.
 * Use this to track data modifications for compliance purposes.
 */
@Documented
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
