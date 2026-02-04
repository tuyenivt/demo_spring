package com.example.database.replication.aspect;

import com.example.database.replication.routing.DataSourceContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

/**
 * Aspect for logging database routing decisions.
 * Logs which datasource (WRITER/READER) is being used for repository operations.
 */
@Slf4j
@Aspect
@Component
public class DatabaseRoutingAspect {

    @Before("execution(* com.example.database.replication.repository.*(..))")
    public void logDatabaseOperation(JoinPoint joinPoint) {
        var dataSourceType = DataSourceContextHolder.getCurrentDataSource();
        log.debug("{} operation via {} datasource: {}.{}",
                dataSourceType,
                dataSourceType,
                joinPoint.getSignature().getDeclaringTypeName(),
                joinPoint.getSignature().getName());
    }
}
