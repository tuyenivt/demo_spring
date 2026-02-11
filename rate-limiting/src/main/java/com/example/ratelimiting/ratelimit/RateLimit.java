package com.example.ratelimiting.ratelimit;

import java.lang.annotation.*;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Repeatable(RateLimits.class)
public @interface RateLimit {

    /**
     * Max requests allowed. Ignored when {@link #profile()} is set.
     */
    long limit() default 0;

    /**
     * Time window in seconds. Ignored when {@link #profile()} is set.
     */
    long durationSeconds() default 0;

    /**
     * Optional profile name defined in {@code rate-limiting.profiles.*}.
     * When set, {@code limit} and {@code durationSeconds} are resolved from configuration.
     */
    String profile() default "";

    /**
     * Token refill strategy.
     */
    RefillStrategy strategy() default RefillStrategy.INTERVALLY;
}
