package com.example.ratelimiting.ratelimit;

import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Aspect
@Component
@RequiredArgsConstructor
public class RateLimitAspect {

    private final RateLimitService rateLimitService;
    private final UserContext userContext;

    @Before("@annotation(rateLimit)")
    public void rateLimit(JoinPoint jp, RateLimit rateLimit) {
        var key = String.join(":", "rate-limit", "user", userContext.getUserId(), jp.getSignature().toShortString());
        if (!rateLimitService.tryConsume(key, rateLimit.limit(), rateLimit.durationSeconds())) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS);
        }
    }
}
