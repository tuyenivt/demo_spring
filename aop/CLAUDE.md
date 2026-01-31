# AOP Subproject

## Overview
Spring AOP demonstration project showcasing Aspect-Oriented Programming concepts using Spring Boot and AspectJ annotations.

## Project Structure
```
aop/
├── build.gradle
└── src/main/java/com/example/aop/
    ├── MainApplication.java          # Spring Boot entry point
    ├── aspect/
    │   ├── CustomAspect.java         # All AOP advice implementations
    │   └── LogExecutionTime.java     # Custom method annotation
    ├── dao/
    │   └── AccountDao.java           # Data access layer (intercepted)
    ├── entity/
    │   └── Account.java              # Domain model
    └── service/
        └── Service.java              # Business logic layer
```

## Key Components

### CustomAspect.java
Main aspect class implementing 6 advice types:
- `@Around` with `@annotation(LogExecutionTime)` - Method execution timing
- `@Before` on `AccountDao.add()` - Pre-execution logging
- `@AfterReturning` on `AccountDao.find()` - Return value modification
- `@AfterThrowing` on `AccountDao.delete()` - Exception logging
- `@After` on find/delete methods - Finally-style cleanup
- `@Around` on `findOrExceptionIfNotFound()` - Exception re-throwing

### LogExecutionTime.java
Custom annotation for marking methods to measure execution time.

## Build & Run
```bash
# Run tests
./gradlew :aop:test

# Run application
./gradlew :aop:bootRun
```

## Dependencies
- `spring-boot-starter-aop` - Core AOP support
- Lombok - Annotation processing
- JUnit 5 - Testing

## Pointcut Patterns Used
- Annotation-based: `@annotation(LogExecutionTime)`
- Execution-based: `execution(public void com.example.aop.dao.AccountDao.add())`
- Wildcard patterns: `execution(* com.example.aop.dao.AccountDao.find(..))`
