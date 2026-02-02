package com.example.ratelimiting.ratelimit;

import jakarta.servlet.http.HttpServletResponse;
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
    private final HttpServletResponse response;

    @Before("@annotation(rateLimit)")
    public void rateLimit(JoinPoint jp, RateLimit rateLimit) {
        var key = String.join(":", "rate-limit", userContext.getIdentifier(), jp.getSignature().toShortString());
        var result = rateLimitService.tryConsumeWithInfo(key, rateLimit.limit(), rateLimit.durationSeconds());

        response.setHeader("X-RateLimit-Limit", String.valueOf(rateLimit.limit()));
        response.setHeader("X-RateLimit-Remaining", String.valueOf(result.remainingTokens()));
        response.setHeader("X-RateLimit-Reset", String.valueOf(result.resetTimeSeconds()));

        if (!result.consumed()) {
            response.setHeader("Retry-After", String.valueOf(result.retryAfterSeconds()));
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS);
        }
    }
}
