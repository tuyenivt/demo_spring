package com.example.ratelimiting.ratelimit;

import com.example.ratelimiting.service.RateLimitService;
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
        var userId = userContext.getUserId();
        var key = String.join(":", "rate-limit", "user", userId, jp.getSignature().toShortString());
        var bucket = rateLimitService.resolveBucket(key, rateLimit.limit(), rateLimit.durationSeconds());
        if (!bucket.tryConsume(1)) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS);
        }
    }
}
