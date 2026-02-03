package com.example.database.replication.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class DatabaseRoutingAspect {

    @Before("execution(* com.example.database.replication.repository.write..*(..))")
    public void logWriteOperation(JoinPoint joinPoint) {
        log.debug("WRITE operation: {}.{}", joinPoint.getSignature().getDeclaringTypeName(), joinPoint.getSignature().getName());
    }

    @Before("execution(* com.example.database.replication.repository.read..*(..))")
    public void logReadOperation(JoinPoint joinPoint) {
        log.debug("READ operation: {}.{}", joinPoint.getSignature().getDeclaringTypeName(), joinPoint.getSignature().getName());
    }
}
