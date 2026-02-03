package com.example.idempotent.idempotent;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class IdempotentAspect {

    private final IdempotentConfig config;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String FIRST_REQUEST = "first-request";

    @Pointcut("@annotation(com.example.idempotent.idempotent.PreventRepeatedRequests)")
    private void forAnnotationPreventRepeatedRequests() {
        // Pointcut declaration
    }

    @Pointcut("@annotation(com.example.idempotent.idempotent.Idempotent)")
    private void forAnnotationIdempotent() {
        // Pointcut declaration
    }

    @Before("forAnnotationPreventRepeatedRequests()")
    public void preventRepeatedRequests(JoinPoint jp) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes.getRequest();
        String clientIdempotentKey = request.getHeader(config.getClientHeaderKey());
        Boolean isReplay = Boolean.parseBoolean(request.getHeader(config.getClientHeaderReplay()));

        log.info("IdempotentAspect idempotent call with {} = {}, {} = {}", config.getClientHeaderKey(), clientIdempotentKey, config.getClientHeaderReplay(), isReplay);
        String cacheName = String.join("_", config.getCacheStoreKey(), jp.getSignature().getName(),
                StringUtils.hasText(clientIdempotentKey) ? clientIdempotentKey : Arrays.toString(jp.getArgs()));
        log.debug("IdempotentAspect idempotent checking cacheName = {}", cacheName);
        Object cache = redisTemplate.opsForValue().get(cacheName);
        if (isReplay || Objects.isNull(cache)) {
            redisTemplate.opsForValue().set(cacheName, FIRST_REQUEST, config.getTimeoutMinutes(), TimeUnit.MINUTES);
        } else {
            throw new IdempotentException("Repeated requests, previous request expired in " + config.getTimeoutMinutes() + " minutes");
        }
    }

    @Around("forAnnotationIdempotent()")
    public Object idempotent(ProceedingJoinPoint joinPoint) throws Throwable {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes.getRequest();
        Assert.notNull(request, "Request can not null");
        log.debug("request: {}", request);

        String ipAddress = request.getHeader("X-FORWARDED-FOR");
        if (!StringUtils.hasText(ipAddress)) {
            ipAddress = request.getRemoteAddr();
        }
        String path = request.getServletPath();
        String clientHeaderKeyVal = request.getHeader(config.getClientHeaderKey());
        String cacheKey = String.join("_", config.getCacheStoreKey(), ipAddress, path, clientHeaderKeyVal);
        log.debug("cacheKey: {}", cacheKey);

        Boolean replay = Boolean.parseBoolean(request.getHeader(config.getClientHeaderReplay()));

        Object keyVal = redisTemplate.opsForValue().get(cacheKey);
        log.debug("keyVal: {}", keyVal);
        if (replay || Objects.isNull(keyVal)) {
            redisTemplate.opsForValue().set(cacheKey, FIRST_REQUEST, config.getTimeoutMinutes(), TimeUnit.MINUTES);
        } else {
            if (FIRST_REQUEST.equalsIgnoreCase(String.valueOf(keyVal))) {
                throw new IdempotentException(String.join("Repeated submissions, previous request expired in ",
                        String.valueOf(redisTemplate.getExpire(cacheKey, TimeUnit.MINUTES)),
                        ", store result in 24h, please retry to get result if it’s ready"));
            }
            return keyVal;
        }
        Object result = joinPoint.proceed();
        log.debug("result: {}", result);
        redisTemplate.opsForValue().set(cacheKey, result, config.getResultExpireMinutes(), TimeUnit.MINUTES);
        return result;
    }
}
