package com.example.aop.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
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
        long start = System.currentTimeMillis();
        System.out.println("joinPoint - around - " + joinPoint.getSignature() + " - begin");
        Object proceed = joinPoint.proceed();
        long executionTime = System.currentTimeMillis() - start;
        System.out.println("joinPoint - around - " + joinPoint.getSignature() + " - executed in " + executionTime + "ms");
        return proceed;
    }
}
