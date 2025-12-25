package com.example.ratelimiting.ratelimit;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RateLimit {

    /**
     * Max requests allowed
     */
    long limit();

    /**
     * Time window in seconds
     */
    long durationSeconds();
}
