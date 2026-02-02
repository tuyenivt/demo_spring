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
    │   ├── CustomAspect.java         # Basic AOP advice implementations
    │   ├── LogExecutionTime.java     # Custom method annotation
    │   ├── LoggingAspect.java        # Enhanced logging with correlation IDs
    │   ├── Audited.java              # Audit annotation
    │   ├── AuditLog.java             # Audit log entry model
    │   ├── AuditAspect.java          # Audit trail aspect
    │   └── PerformanceAspect.java    # Performance monitoring aspect
    ├── dao/
    │   └── AccountDao.java           # Data access layer (intercepted)
    ├── entity/
    │   └── Account.java              # Domain model
    └── service/
        └── Service.java              # Business logic layer
```

## Key Components

### CustomAspect.java
Basic aspect class implementing 6 advice types:
- `@Around` with `@annotation(LogExecutionTime)` - Method execution timing
- `@Before` on `AccountDao.add()` - Pre-execution logging
- `@AfterReturning` on `AccountDao.find()` - Return value modification
- `@AfterThrowing` on `AccountDao.delete()` - Exception logging
- `@After` on find/delete methods - Finally-style cleanup
- `@Around` on `findOrExceptionIfNotFound()` - Exception re-throwing

### LoggingAspect.java
Enhanced logging aspect with:
- Correlation IDs for request tracing (MDC-based)
- Automatic logging of method entry/exit for `@RestController` classes
- Execution time tracking
- Exception logging with correlation context

### AuditAspect.java
Audit trail aspect for compliance tracking:
- Uses `@Audited(action, entity)` annotation
- Logs successful operations with `@AfterReturning`
- Logs failed operations with `@AfterThrowing`
- Captures user, timestamp, method, args, and result

### PerformanceAspect.java
Performance monitoring for `@Service` classes:
- Tracks method execution time
- Logs warnings for slow methods (>1000ms threshold)
- Ready for Micrometer metrics integration

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
- Annotation-based: `@annotation(LogExecutionTime)`, `@annotation(audited)`
- Class-level annotation: `@within(org.springframework.web.bind.annotation.RestController)`, `@within(org.springframework.stereotype.Service)`
- Execution-based: `execution(public void com.example.aop.dao.AccountDao.add())`
- Wildcard patterns: `execution(* com.example.aop.dao.AccountDao.find(..))`

## Usage Examples

### @Audited Annotation
```bash
@Audited(action = "DELETE", entity = "Account")
public void deleteAccount(Long id) { ... }
```

### @LogExecutionTime Annotation
```bash
@LogExecutionTime
public void processData() { ... }
```
