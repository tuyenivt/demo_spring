package com.example.ratelimiting.ratelimit;

import java.lang.annotation.*;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RateLimits {

    RateLimit[] value();
}
