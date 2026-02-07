package com.example.aop.aspect.cache;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Aspect that caches method return values based on method signature and arguments.
 * Demonstrates how @Around advice can conditionally skip proceed().
 */
@Slf4j
@Aspect
@Component
public class CacheAspect {

    private final ConcurrentHashMap<String, Object> cache = new ConcurrentHashMap<>();

    @Around("@annotation(com.example.aop.aspect.cache.SimpleCache)")
    public Object cacheResult(ProceedingJoinPoint joinPoint) throws Throwable {
        var key = joinPoint.getSignature().toShortString() + Arrays.toString(joinPoint.getArgs());

        if (cache.containsKey(key)) {
            log.info("CACHE HIT: {} - returning cached result", key);
            return cache.get(key);
        }

        log.info("CACHE MISS: {} - executing method", key);
        var result = joinPoint.proceed();
        if (result != null) {
            cache.put(key, result);
        }
        return result;
    }

    public void clearCache() {
        cache.clear();
    }
}
