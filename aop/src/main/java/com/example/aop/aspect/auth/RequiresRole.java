package com.example.aop.aspect.auth;

import java.lang.annotation.*;

/**
 * Annotation to enforce role-based access control on methods.
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequiresRole {

    /**
     * The role required to execute the annotated method (e.g., "ADMIN").
     */
    String value();
}
