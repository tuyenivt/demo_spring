package com.example.aop.aspect.cache;

import java.lang.annotation.*;

/**
 * Custom caching annotation (not Spring's @Cacheable).
 * Caches method return values based on their arguments.
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface SimpleCache {
}
