# Spring AOP Demo

Demonstration of Aspect-Oriented Programming (AOP) concepts using Spring Boot and AspectJ annotations.

## Features

This project showcases:

- **All 5 AspectJ Advice Types**: `@Before`, `@After`, `@AfterReturning`, `@AfterThrowing`, `@Around`
- **Enhanced Logging**: Correlation ID-based request tracing for controllers
- **Audit Trail**: Track data modifications with `@Audited` annotation
- **Performance Monitoring**: Automatic slow method detection for services

## Quick Start

```bash
# Run the application
./gradlew :aop:bootRun

# Run tests
./gradlew :aop:test
```

## Aspects Overview

### CustomAspect
Basic AOP demonstrations covering all advice types:

```bash
// Around - Method execution timing
@Around("@annotation(LogExecutionTime)")
public Object logExecutionTime(ProceedingJoinPoint joinPoint) { ... }

// Before - Pre-execution logging
@Before("execution(public void com.example.aop.dao.AccountDao.add())")
public void beforeAddAccount() { ... }

// AfterReturning - Modify return values
@AfterReturning(pointcut = "...", returning = "result")
public void afterReturningFindAccountAdvice(JoinPoint joinPoint, List<Account> result) { ... }

// AfterThrowing - Exception logging
@AfterThrowing(pointcut = "...", throwing = "exception")
public void afterThrowingDeleteAccountAdvice(JoinPoint joinPoint, Throwable exception) { ... }

// After - Finally-style cleanup
@After("forAccountDaoFind() || forAccountDaoDelete()")
public void afterFindOrDeleteAccount(JoinPoint joinPoint) { ... }
```

### LoggingAspect
Enhanced logging with correlation IDs for REST controllers:

```bash
@Around("@within(org.springframework.web.bind.annotation.RestController)")
public Object logControllerMethods(ProceedingJoinPoint joinPoint) throws Throwable {
    String correlationId = UUID.randomUUID().toString().substring(0, 8);
    MDC.put("correlationId", correlationId);
    // Log entry, execute, log exit with duration
}
```

**Output example:**
```
[a1b2c3d4] Entering: AccountController.getAccount(..) with args: [123]
[a1b2c3d4] Exiting: AccountController.getAccount(..) in 45ms with result: Account(id=123)
```

### AuditAspect
Track data modifications for compliance:

```bash
// Annotate methods to audit
@Audited(action = "DELETE", entity = "Account")
public void deleteAccount(Long id) { ... }

@Audited(action = "CREATE", entity = "User")
public User createUser(UserDto dto) { ... }
```

**Output example:**
```
AUDIT: AuditLog(timestamp=2024-01-15T10:30:00Z, user=john.doe, action=DELETE, entity=Account, method=AccountService.deleteAccount(..), args=[123], result=null)
```

### PerformanceAspect
Monitor slow methods in service layer:

```bash
@Around("@within(org.springframework.stereotype.Service)")
public Object monitorPerformance(ProceedingJoinPoint joinPoint) throws Throwable {
    // Logs warning if method takes > 1000ms
}
```

**Output example:**
```
WARN  SLOW METHOD: OrderService.processOrder(..) took 2341ms (threshold: 1000ms)
```

## Annotations

| Annotation          | Target | Description                       |
|---------------------|--------|-----------------------------------|
| `@LogExecutionTime` | Method | Measure and log execution time    |
| `@Audited`          | Method | Record audit trail for compliance |

## Pointcut Patterns

```bash
// Annotation-based (method level)
@annotation(LogExecutionTime)
@annotation(audited)

// Class-level annotation
@within(org.springframework.web.bind.annotation.RestController)
@within(org.springframework.stereotype.Service)

// Execution patterns
execution(public void com.example.aop.dao.AccountDao.add())
execution(* com.example.aop.dao.AccountDao.find(..))
```

## Key Concepts

### Proxy Limitations
Spring AOP uses proxy-based interception. Self-invocation (calling methods within the same class) bypasses the proxy:

```bash
@Service
public class MyService {
    public void methodA() {
        methodB(); // This call BYPASSES aspects!
    }

    @LogExecutionTime
    public void methodB() { ... }
}
```

### MDC (Mapped Diagnostic Context)
LoggingAspect uses SLF4J MDC to propagate correlation IDs across log statements. Configure your logging pattern to include it:

```properties
logging.pattern.console=%d{HH:mm:ss} [%X{correlationId}] %-5level %logger{36} - %msg%n
```

## Dependencies

```gradle
dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-aop'
    compileOnly 'org.projectlombok:lombok'
    annotationProcessor 'org.projectlombok:lombok'
}
```
