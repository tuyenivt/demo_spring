package com.example.aop.aspect;

import java.util.Arrays;
import java.util.List;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.example.aop.entity.Account;

@Aspect
@Component
public class CustomAspect {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    // Pointcut declaration
    @Pointcut("@annotation(LogExecutionTime)")
    private void forAnnotationLogExecutionTime() {}

    // Around Advice
    @Around("forAnnotationLogExecutionTime()")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        // get method signature
        Signature signature = joinPoint.getSignature();
        // get method arguments
        Object[] args = joinPoint.getArgs();
        logger.info("joinPoint - around - signature:" + signature + " - arguments:" + Arrays.deepToString(args));
        long start = System.currentTimeMillis();
        Object proceed = joinPoint.proceed();
        long executionTime = System.currentTimeMillis() - start;
        logger.info("joinPoint - around - signature:" + signature + " - executed in " + executionTime + "ms");
        return proceed;
    }

    // Before Advice
    @Before("execution(public void com.example.aop.dao.AccountDao.add())")
    public void beforeAddAccount() {
        logger.info("beforeAddAccount - executing Before Advice on AccountDao.add()");
    }

    @Pointcut("execution(* com.example.aop.dao.AccountDao.find(..))")
    private void forAccountDaoFind() {}

    // AfterReturning Advice
    @AfterReturning(pointcut = "forAccountDaoFind()", returning = "result")
    public void afterReturningFindAccountAdvice(JoinPoint joinPoint, List<Account> result) {
        logger.info("afterReturningFindAccount - executing AfterReturning Advice on AccountDao.find() with result is " + result);
        result.add(new Account(1, "Tom"));
    }

    @Pointcut("execution(* com.example.aop.dao.AccountDao.delete(*))")
    private void forAccountDaoDelete() {}

    // AfterThrowing Advice
    // the exception is still propagated back to AOP proxy, and then the exception is propagated back to the main application
    // if you want to stop the exception propagation then use the @Around advice
    @AfterThrowing(pointcut = "forAccountDaoDelete()", throwing = "exception")
    public void afterThrowingDeleteAccountAdvice(JoinPoint joinPoint, Throwable exception) {
        logger.info("afterThrowingDeleteAccount - executing AfterThrowing Advice on AccountDao.delete() - exception: " + exception);
    }

    // After Advice : look like finally, will execute even success or exception
    // After Advice will execute before after returning advice or after throwing advice
    @After("forAccountDaoFind() || forAccountDaoDelete()")
    public void afterFindOrDeleteAccount(JoinPoint joinPoint) {
        logger.info("afterFindOrDeleteAccount - executing After Advice on " + joinPoint.getSignature());
    }

    // Around Advice : rethrow exception
    @Around("execution(* com.example.aop.dao.AccountDao.findOrExceptionIfNotFound(..))")
    public Object aroundFindOrExceptionIfNotFoundAdvice(ProceedingJoinPoint joinPoint) throws Throwable {
        Object proceed = null;
        try {
            proceed = joinPoint.proceed();
        } catch (Exception e) {
            logger.info("aroundFindOrExceptionIfNotFoundAdvice - executing Around Advice on AccountDao.FindOrExceptionIfNotFound() - exception: " + e);
            logger.info("aroundFindOrExceptionIfNotFoundAdvice - rethrowing exception");
            throw e;
        }
        return proceed;
    }

}
