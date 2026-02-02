package com.example.aop.aspect;

import com.example.aop.entity.Account;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Slf4j
@Aspect
@Component
public class CustomAspect {

    // Pointcut declaration
    @Pointcut("@annotation(LogExecutionTime)")
    private void forAnnotationLogExecutionTime() {
    }

    // Around Advice
    @Around("forAnnotationLogExecutionTime()")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        // get method signature
        var signature = joinPoint.getSignature();
        // get method arguments
        var args = joinPoint.getArgs();
        log.info("joinPoint - around - signature: {} - arguments: {}", signature, Arrays.deepToString(args));
        var start = System.currentTimeMillis();
        var proceed = joinPoint.proceed();
        var executionTime = System.currentTimeMillis() - start;
        log.info("joinPoint - around - signature: {} - executed in {} ms", signature, executionTime);
        return proceed;
    }

    // Before Advice
    @Before("execution(public void com.example.aop.dao.AccountDao.add())")
    public void beforeAddAccount() {
        log.info("beforeAddAccount - executing Before Advice on AccountDao.add()");
    }

    @Pointcut("execution(* com.example.aop.dao.AccountDao.find(..))")
    private void forAccountDaoFind() {
    }

    // AfterReturning Advice
    @AfterReturning(pointcut = "forAccountDaoFind()", returning = "result")
    public void afterReturningFindAccountAdvice(JoinPoint joinPoint, List<Account> result) {
        log.info("afterReturningFindAccount - executing AfterReturning Advice on AccountDao.find() with result is {}", result);
        result.add(new Account(1, "Tom"));
    }

    @Pointcut("execution(* com.example.aop.dao.AccountDao.delete(*))")
    private void forAccountDaoDelete() {
    }

    // AfterThrowing Advice
    // the exception is still propagated back to AOP proxy, and then the exception is propagated back to the main application
    // if you want to stop the exception propagation then use the @Around advice
    @AfterThrowing(pointcut = "forAccountDaoDelete()", throwing = "exception")
    public void afterThrowingDeleteAccountAdvice(JoinPoint joinPoint, Throwable exception) {
        log.error("afterThrowingDeleteAccount - executing AfterThrowing Advice on AccountDao.delete() - exception:", exception);
    }

    // After Advice : look like finally, will execute even success or exception
    // After Advice will execute before after returning advice or after throwing advice
    @After("forAccountDaoFind() || forAccountDaoDelete()")
    public void afterFindOrDeleteAccount(JoinPoint joinPoint) {
        log.info("afterFindOrDeleteAccount - executing After Advice on {}", joinPoint.getSignature());
    }

    // Around Advice : rethrow exception
    @Around("execution(* com.example.aop.dao.AccountDao.findOrExceptionIfNotFound(..))")
    public Object aroundFindOrExceptionIfNotFoundAdvice(ProceedingJoinPoint joinPoint) throws Throwable {
        Object proceed = null;
        try {
            proceed = joinPoint.proceed();
        } catch (Exception e) {
            log.error("aroundFindOrExceptionIfNotFoundAdvice - executing Around Advice on AccountDao.FindOrExceptionIfNotFound() - exception:", e);
            log.info("aroundFindOrExceptionIfNotFoundAdvice - rethrowing exception");
            throw e;
        }
        return proceed;
    }
}
