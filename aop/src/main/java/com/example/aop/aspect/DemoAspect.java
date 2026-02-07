package com.example.aop.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Educational aspect demonstrating all 5 AspectJ advice types and pointcut composition.
 * This aspect exists purely for learning purposes - showing how different advice types work.
 * <p>
 * In production code, prefer annotation-driven aspects (ExecutionLoggingAspect, etc.)
 * over hardcoded execution pointcuts like these.
 */
@Slf4j
@Aspect
@Order(10) // Low priority - demonstration only
@Component
public class DemoAspect {

    // --- POINTCUT DECLARATIONS ---

    @Pointcut("execution(* com.example.aop.dao.AccountDao.find(..))")
    private void accountDaoFind() {
    }

    @Pointcut("execution(* com.example.aop.dao.AccountDao.delete(*))")
    private void accountDaoDelete() {
    }

    // --- POINTCUT COMPOSITION EXAMPLES ---

    /**
     * AND (&&): Matches methods in @Service classes that also have @ExecutionLogging.
     * Demonstrates combining class-level and method-level pointcuts.
     */
    @Pointcut("@within(org.springframework.stereotype.Service) && @annotation(ExecutionLogging)")
    private void serviceWithExecutionLogging() {
    }

    /**
     * NOT (!): Matches all DAO package methods except entity package.
     * Demonstrates exclusion patterns.
     */
    @Pointcut("execution(* com.example.aop.dao..*(..)) && !execution(* com.example.aop.entity..*(..))")
    private void daoMethodsExcludingEntities() {
    }

    // --- ADVICE TYPE DEMONSTRATIONS ---

    /**
     * @Before - Executes before the method call.
     * Cannot prevent method execution or modify arguments.
     */
    @Before("execution(public void com.example.aop.dao.AccountDao.add())")
    public void beforeAddAccount() {
        log.info("[DEMO @Before] About to execute AccountDao.add()");
    }

    /**
     * @AfterReturning - Executes after successful method return.
     * Can access the return value but cannot modify it (should not mutate collections).
     */
    @AfterReturning(pointcut = "accountDaoFind()", returning = "result")
    public void afterReturningFindAccount(JoinPoint joinPoint, List<?> result) {
        log.info("[DEMO @AfterReturning] AccountDao.find() returned: {} items", result.size());
    }

    /**
     * @AfterThrowing - Executes when method throws exception.
     * Can access the exception but cannot suppress it.
     */
    @AfterThrowing(pointcut = "accountDaoDelete()", throwing = "exception")
    public void afterThrowingDeleteAccount(JoinPoint joinPoint, Throwable exception) {
        log.error("[DEMO @AfterThrowing] AccountDao.delete() threw exception: {}", exception.getMessage());
    }

    /**
     * @After - Executes after method completes (success or exception).
     * Similar to finally block. Runs BEFORE @AfterReturning or @AfterThrowing.
     */
    @After("accountDaoFind() || accountDaoDelete()")
    public void afterFindOrDeleteAccount(JoinPoint joinPoint) {
        log.info("[DEMO @After] Cleanup after {}", joinPoint.getSignature().toShortString());
    }

    /**
     * @Around - Most powerful advice. Can:
     * - Control method execution (prevent, retry, cache)
     * - Modify arguments via proceed(args)
     * - Modify return value
     * - Suppress exceptions or throw new ones
     */
    @Around("execution(* com.example.aop.dao.AccountDao.findOrExceptionIfNotFound(..))")
    public Object aroundFindOrExceptionIfNotFound(ProceedingJoinPoint joinPoint) throws Throwable {
        log.info("[DEMO @Around] Before proceeding to {}", joinPoint.getSignature().toShortString());
        try {
            var result = joinPoint.proceed();
            log.info("[DEMO @Around] Method succeeded, returning result");
            return result;
        } catch (Exception e) {
            log.error("[DEMO @Around] Method threw exception, rethrowing: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Demonstrates pointcut composition with AND (&&).
     * Matches only when BOTH conditions are true.
     */
    @Before("serviceWithExecutionLogging()")
    public void beforeServiceExecutionLogging(JoinPoint joinPoint) {
        log.info("[DEMO POINTCUT &&] Matched @Service + @ExecutionLogging on {}",
                joinPoint.getSignature().toShortString());
    }
}
