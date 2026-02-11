package com.example.ratelimiting.ratelimit;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.List;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class RateLimitAspect {

    private final RateLimitService rateLimitService;
    private final RateLimitProperties rateLimitProperties;
    private final UserContext userContext;
    private final HttpServletResponse response;

    @Before("@annotation(com.example.ratelimiting.ratelimit.RateLimit) " +
            "|| @annotation(com.example.ratelimiting.ratelimit.RateLimits) " +
            "|| @within(com.example.ratelimiting.ratelimit.RateLimit) " +
            "|| @within(com.example.ratelimiting.ratelimit.RateLimits)")
    public void rateLimit(JoinPoint jp) {
        var limits = resolveRateLimits(jp);
        if (limits.isEmpty()) return;

        var methodKey = jp.getSignature().toShortString();
        var identifier = userContext.getIdentifier();

        // For header reporting, use the first (most restrictive) limit
        var primary = limits.getFirst();
        resolveParams(primary);

        try {
            for (var rateLimit : limits) {
                var params = resolveParams(rateLimit);
                var key = String.join(":", "rate-limit", identifier, methodKey,
                        String.valueOf(params.durationSeconds()));

                var result = rateLimitService.tryConsumeWithInfo(key, params.limit(), params.durationSeconds(), params.strategy());

                if (limits.indexOf(rateLimit) == 0) {
                    response.setHeader("X-RateLimit-Limit", String.valueOf(params.limit()));
                    response.setHeader("X-RateLimit-Remaining", String.valueOf(result.remainingTokens()));
                    response.setHeader("X-RateLimit-Reset", String.valueOf(result.resetTimeSeconds()));
                }

                if (!result.consumed()) {
                    response.setHeader("Retry-After", String.valueOf(result.retryAfterSeconds()));
                    log.warn("Rate limit exceeded: key={}, retryAfter={}s", key, result.retryAfterSeconds());
                    throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS);
                }
            }
        } catch (ResponseStatusException ex) {
            throw ex;
        } catch (Exception ex) {
            log.warn("Rate limiting unavailable, allowing request: {}", ex.getMessage());
        }
    }

    private List<RateLimit> resolveRateLimits(JoinPoint jp) {
        var method = ((MethodSignature) jp.getSignature()).getMethod();
        var targetClass = jp.getTarget().getClass();

        // Method-level @RateLimits (repeatable container)
        var methodRateLimits = method.getAnnotation(RateLimits.class);
        if (methodRateLimits != null) {
            return Arrays.asList(methodRateLimits.value());
        }

        // Method-level @RateLimit (single)
        var methodRateLimit = method.getAnnotation(RateLimit.class);
        if (methodRateLimit != null) {
            return List.of(methodRateLimit);
        }

        // Class-level @RateLimits (repeatable container)
        var classRateLimits = targetClass.getAnnotation(RateLimits.class);
        if (classRateLimits != null) {
            return Arrays.asList(classRateLimits.value());
        }

        // Class-level @RateLimit (single)
        var classRateLimit = targetClass.getAnnotation(RateLimit.class);
        if (classRateLimit != null) {
            return List.of(classRateLimit);
        }

        return List.of();
    }

    private ResolvedParams resolveParams(RateLimit rateLimit) {
        if (!rateLimit.profile().isBlank()) {
            var profile = rateLimitProperties.getProfiles().get(rateLimit.profile());
            if (profile == null) {
                throw new IllegalArgumentException("Unknown rate-limit profile: " + rateLimit.profile());
            }
            return new ResolvedParams(profile.getLimit(), profile.getDurationSeconds(), profile.getStrategy());
        }
        return new ResolvedParams(rateLimit.limit(), rateLimit.durationSeconds(), rateLimit.strategy());
    }

    private record ResolvedParams(long limit, long durationSeconds, RefillStrategy strategy) {
    }
}
