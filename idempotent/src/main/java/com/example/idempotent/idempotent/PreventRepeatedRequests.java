package com.example.idempotent.idempotent;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface PreventRepeatedRequests {
    /**
     * Timeout for duplicate detection window. -1 = use global config.
     */
    long timeout() default -1;

    /**
     * Time unit for timeout. Defaults to MINUTES.
     */
    TimeUnit timeUnit() default TimeUnit.MINUTES;
}
