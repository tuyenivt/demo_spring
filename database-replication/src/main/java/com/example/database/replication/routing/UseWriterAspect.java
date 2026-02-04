package com.example.database.replication.routing;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Aspect that intercepts methods annotated with {@link UseWriter}
 * and routes them to the writer datasource.
 *
 * <p>This aspect has higher precedence than @Transactional (Ordered.LOWEST_PRECEDENCE - 10)
 * to ensure the datasource context is set before the transaction begins.
 *
 * <p>Uses ScopedValue for context management which automatically handles cleanup.
 */
@Slf4j
@Aspect
@Component
@Order(Ordered.LOWEST_PRECEDENCE - 10)
public class UseWriterAspect {

    @Around("@annotation(useWriter)")
    public Object routeToWriter(ProceedingJoinPoint joinPoint, UseWriter useWriter) {
        log.debug("Routing to WRITER datasource for method: {}.{}",
                joinPoint.getSignature().getDeclaringTypeName(),
                joinPoint.getSignature().getName());

        return DataSourceContextHolder.callWithWriter(() -> {
            try {
                return joinPoint.proceed();
            } catch (RuntimeException | Error e) {
                throw e;
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        });
    }
}
