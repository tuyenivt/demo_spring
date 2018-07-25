package com.example.aop.aspect;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD) // where our annotation will be applicable. Here it will only work on methods
@Retention(RetentionPolicy.RUNTIME) // the annotation will be available to the JVM at runtime or not
public @interface LogExecutionTime {

}
