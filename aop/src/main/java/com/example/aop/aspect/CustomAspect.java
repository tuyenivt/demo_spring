package com.example.aop.aspect;

import java.util.Arrays;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class CustomAspect {

    // Pointcut declaration
	@Pointcut("@annotation(LogExecutionTime)")
	private void forAnnotationLogExecutionTime() {}

	// Advice
    @Around("forAnnotationLogExecutionTime()")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
    	// get method signature
    	Signature signature = joinPoint.getSignature();
    	// get method arguments
    	Object[] args = joinPoint.getArgs();
    	System.out.println("joinPoint - around - signature:" + signature + " - arguments:" + Arrays.deepToString(args));
        long start = System.currentTimeMillis();
        Object proceed = joinPoint.proceed();
        long executionTime = System.currentTimeMillis() - start;
        System.out.println("joinPoint - around - signature:" + signature + " - executed in " + executionTime + "ms");
        return proceed;
    }
}
