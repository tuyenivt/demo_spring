# Spring AOP Demo

Production-ready demonstration of Aspect-Oriented Programming (AOP) using annotation-driven aspects with clear separation of concerns.

## Features

### Production Aspects (Annotation-Driven)
- **Execution Logging** (`@ExecutionLogging`) - Detailed method logging with timing
- **Performance Monitoring** (`@MonitorPerformance`) - Configurable slow method detection
- **Retry Pattern** (`@Retryable`) - Automatic retry on failure
- **Method Caching** (`@SimpleCache`) - Simple in-memory cache
- **Authorization** (`@RequiresRole`) - Role-based access control
- **Audit Trail** (`@Audited`) - Compliance logging

### Layer-Specific Aspects
- **Controller Logging** - Automatic correlation ID tracking for `@RestController`

### Educational Aspects
- **All 5 AspectJ Advice Types** - `@Before`, `@After`, `@AfterReturning`, `@AfterThrowing`, `@Around`
- **Pointcut Composition** - `&&` (AND), `||` (OR), `!` (NOT) examples
- **Self-Invocation Demo** - Shows proxy limitation

## Quick Start

```bash
# Run tests
./gradlew :aop:test

# Run application
./gradlew :aop:bootRun
```

## Architecture

### Design Principles
1. **Annotation-Driven** - Explicit opt-in via annotations (not blanket execution pointcuts)
2. **Single Responsibility** - Each aspect has ONE clear purpose
3. **Production-Ready** - Configurable, testable, maintainable
4. **Educational Separation** - Demo code isolated from production patterns
5. **Package by Feature** - Related files grouped by concern (audit/, auth/, cache/, retry/)

### Aspect Structure

```
┌─────────────────────────────────────────────────────────────┐
│ Production Aspects (Annotation-Driven, Opt-In)              │
├─────────────────────────────────────────────────────────────┤
│ ExecutionLoggingAspect       @ExecutionLogging              │
│ PerformanceMonitoringAspect  @MonitorPerformance            │
│ RetryAspect                  @Retryable                     │
│ CacheAspect                  @SimpleCache                   │
│ AuthorizationAspect          @RequiresRole                  │
│ AuditAspect                  @Audited                       │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│ Layer-Specific Aspects (Automatic)                          │
├─────────────────────────────────────────────────────────────┤
│ ControllerLoggingAspect      Auto @RestController           │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│ Educational Aspects (Demonstrations Only)                   │
├─────────────────────────────────────────────────────────────┤
│ DemoAspect       All 5 advice types + pointcuts │
└─────────────────────────────────────────────────────────────┘
```

## Project Structure

```
aop/src/main/java/com/example/aop/
├── aspect/
│   ├── audit/                         Audit Concern
│   │   ├── AuditAspect.java           - @AfterReturning/@AfterThrowing
│   │   ├── Audited.java               - Audit annotation
│   │   └── AuditLog.java              - Audit log model
│   │
│   ├── auth/                          Authorization Concern
│   │   ├── AuthorizationAspect.java   - @Before RBAC check
│   │   ├── RequiresRole.java          - Role annotation
│   │   ├── SecurityContext.java       - ThreadLocal role holder
│   │   └── AccessDeniedException.java - Auth exception
│   │
│   ├── cache/                         Caching Concern
│   │   ├── CacheAspect.java           - @Around with ConcurrentHashMap
│   │   └── SimpleCache.java           - Cache annotation
│   │
│   ├── retry/                         Retry Concern
│   │   ├── RetryAspect.java           - @Around retry logic (@Order 0)
│   │   └── Retryable.java             - Retry annotation
│   │
│   ├── ControllerLoggingAspect.java        Layer: @RestController (@Order 1)
│   ├── ExecutionLoggingAspect.java         Core: @ExecutionLogging (@Order 2)
│   ├── ExecutionLogging.java               - Execution logging annotation
│   ├── PerformanceMonitoringAspect.java    Core: @MonitorPerformance (@Order 3)
│   ├── MonitorPerformance.java             - Performance annotation
│   └── DemoAspect.java                     Educational: All 5 advice types (@Order 10)
│
├── dao/AccountDao.java
├── entity/Account.java
└── service/AccountService.java
```

**Organization Benefits**:
- **Feature Packages**: Each cross-cutting concern isolated in its own subpackage
- **Easier Navigation**: Related files (aspect + annotation + models) co-located
- **Testability**: Can test each concern independently
- **Maintainability**: Clear boundaries between concerns

---

## Core Aspects

### ExecutionLoggingAspect

Detailed execution logging for methods annotated with `@ExecutionLogging`.

**Usage**:
```java
@Service
public class AccountService {
    @ExecutionLogging
    public void processOrder(Order order) {
        // Business logic
    }
}
```

**Output**:
```
INFO  Executing: AccountService.processOrder(..) with args: [Order(id=123)]
INFO  Completed: AccountService.processOrder(..) in 45ms with result: null
```

**Features**:
- Method signature + arguments
- Execution time tracking
- Return value logging (truncated > 100 chars)
- Exception logging with timing

---

### PerformanceMonitoringAspect

Detects slow methods via `@MonitorPerformance` annotation.

**Usage**:
```java
// Method-specific threshold
@MonitorPerformance(thresholdMs = 500)
public void criticalOperation() { ... }

// Or use global threshold from config
@MonitorPerformance
public void regularOperation() { ... }
```

**Configuration** (`application.properties`):
```properties
aop.performance.slow-threshold-ms=1000
```

**Output**:
```
WARN  SLOW METHOD: AccountService.criticalOperation(..) took 850ms (threshold: 500ms)
```

---

### ControllerLoggingAspect

Automatic logging for all `@RestController` methods with distributed tracing support.

**Target**: Automatically applied to all `@RestController` classes (no annotation needed)

**Features**:
- Unique correlation ID per request
- Entry/exit logging with args/results
- Request duration tracking
- MDC propagation for distributed tracing

**Output**:
```
INFO  [a1b2c3d4] Entering: UserController.getUser(..) with args: [123]
INFO  [a1b2c3d4] Exiting: UserController.getUser(..) in 45ms with result: User(id=123)
```

**MDC Configuration**:
```properties
logging.pattern.console=%d{HH:mm:ss} [%X{correlationId}] %-5level %logger{36} - %msg%n
```

---

### DemoAspect

Educational demonstration of all AspectJ advice types. **For learning only** - not production patterns.

**Demonstrates**:

```java
// @Before - Runs before method
[DEMO @Before] About to execute AccountDao.add()

// @AfterReturning - Access return value
[DEMO @AfterReturning] AccountDao.find() returned: 2 items

// @AfterThrowing - Handle exceptions
[DEMO @AfterThrowing] AccountDao.delete() threw exception: Access denied

// @After - Finally-style cleanup
[DEMO @After] Cleanup after AccountDao.find(..)

// @Around - Full control (can prevent execution)
[DEMO @Around] Before proceeding to AccountDao.findOrExceptionIfNotFound(..)
[DEMO @Around] Method succeeded, returning result
```

**Pointcut Composition Examples**:
```java
// AND (&&) - Methods in @Service with @ExecutionLogging
@Pointcut("@within(Service) && @annotation(ExecutionLogging)")

// NOT (!) - DAO methods excluding entity package
@Pointcut("execution(* com.example.aop.dao..*(..)) && !execution(* com.example.aop.entity..*(..))")
```

---

## Supporting Aspects

### RetryAspect
Automatic retry on failure with exponential backoff.

```java
@Retryable(maxAttempts = 3, retryOn = RuntimeException.class)
public Data fetchFromUnreliableService() { ... }
```

**Output**:
```
WARN  Attempt 1/3 failed: Connection timeout
INFO  Retry attempt 2/3
INFO  Retry succeeded on attempt 2
```

---

### CacheAspect
Method-level caching with ConcurrentHashMap.

```java
@SimpleCache
public User getUserById(Long id) { ... }
```

**Output**:
```
CACHE MISS: UserService.getUserById(..) - executing method
CACHE HIT: UserService.getUserById(..) - returning cached result
```

---

### AuthorizationAspect
Role-based access control using ThreadLocal security context.

```java
@RequiresRole("ADMIN")
public void deleteUser(Long id) { ... }
```

**Usage**:
```java
SecurityContext.setRole("ADMIN");
userService.deleteUser(123); // Allowed

SecurityContext.setRole("USER");
userService.deleteUser(123); // Throws AccessDeniedException
```

---

### AuditAspect
Track data modifications for compliance.

```java
@Audited(action = "DELETE", entity = "User")
public void deleteUser(Long id) { ... }
```

---

## Annotations Reference

| Annotation            | Target       | Description                                  |
|-----------------------|--------------|----------------------------------------------|
| `@ExecutionLogging`   | Method       | Detailed execution logging                   |
| `@MonitorPerformance` | Method, Type | Slow method detection (opt-in)               |
| `@Audited`            | Method       | Record audit trail                           |
| `@Retryable`          | Method       | Retry on failure                             |
| `@SimpleCache`        | Method       | Cache return value                           |
| `@RequiresRole`       | Method       | Enforce role-based access                    |

## Aspect Ordering

Execution order (lower `@Order` = higher priority):

| Order | Aspect                        | Purpose                 |
|-------|-------------------------------|-------------------------|
| 0     | `RetryAspect`                 | Outermost retry wrapper |
| 1     | `ControllerLoggingAspect`     | Correlation IDs         |
| 2     | `ExecutionLoggingAspect`      | Detailed method logging |
| 3     | `PerformanceMonitoringAspect` | Slow method warnings    |
| 10    | `DemoAspect`                  | Educational demos       |

## Key Concepts

### Self-Invocation Proxy Limitation

Spring AOP uses proxies. Internal method calls bypass the proxy:

```java
@Service
public class OrderService {
    // This DOES NOT work - aspect bypassed
    public void processBatch(List<Order> orders) {
        for (Order order : orders) {
            processOne(order); // Direct call - no aspect!
        }
    }

    @ExecutionLogging
    public void processOne(Order order) { ... }
}
```

**Why?** The call `processOne(order)` is actually `this.processOne(order)`, which calls the target object directly, not through the AOP proxy.

**Workarounds**:
1. Inject self: `@Autowired @Lazy OrderService self; self.processOne(order);`
2. Use `AopContext.currentProxy()` (requires `@EnableAspectJAutoProxy(exposeProxy = true)`)
3. Move method to separate bean (cleanest)

### Pointcut Patterns

```java
// Annotation-based (RECOMMENDED for production)
@annotation(ExecutionLogging)
@within(RestController)

// Execution-based (use for demos/learning)
execution(public void com.example.aop.dao.AccountDao.add())
execution(* com.example.aop.dao..*(..))

// Composition
@Pointcut("@within(Service) && @annotation(ExecutionLogging)")  // AND
@Pointcut("find() || delete()")                                  // OR
@Pointcut("execution(* dao..*(..)) && !execution(* entity..*(..))")  // NOT
```

## Testing

All aspects have dedicated unit tests using `OutputCaptureExtension`:

```bash
./gradlew :aop:test
```

Test structure:
- `ExecutionLoggingAspectTest` - Verify @ExecutionLogging
- `PerformanceMonitoringAspectTest` - Verify slow detection
- `DemoAspectTest` - Verify all 5 advice types
- `SelfInvocationTest` - Demonstrate proxy limitation
- `RetryAspectTest`, `CacheAspectTest`, `AuthorizationAspectTest` - Supporting aspects

## Dependencies

```gradle
dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-aop'
    compileOnly 'org.projectlombok:lombok'
    annotationProcessor 'org.projectlombok:lombok'
}
```

## Migration Guide

### Before (Tightly Coupled)
```java
// CustomAspect - Mixed concerns
@Aspect
public class CustomAspect {
    // @ExecutionLogging handling
    // Hardcoded DAO pointcuts
    // Pointcut demos
}

// PerformanceAspect - Blanket monitoring
@Around("@within(Service)")  // All services monitored!
```

### After (Clean Separation)
```java
// ExecutionLoggingAspect - Single purpose
@Aspect
public class ExecutionLoggingAspect {
    @Around("@annotation(ExecutionLogging)")
    // Only handles @ExecutionLogging
}

// PerformanceMonitoringAspect - Opt-in
@Around("@annotation(MonitorPerformance)")  // Explicit opt-in
```

**Benefits**:
- Clear single responsibility
- Easy to enable/disable per method
- Better testability
- Production-ready patterns
- Educational code isolated

## Best Practices

1. **Prefer annotations over execution pointcuts** for production code
2. **Use `@Order` explicitly** to control aspect execution order
3. **Keep aspects focused** - one concern per aspect
4. **Test aspects independently** using `OutputCaptureExtension`
5. **Document aspect behavior** clearly in javadocs
6. **Avoid self-invocation** - refactor to separate beans if needed
7. **Use MDC for correlation IDs** in distributed systems
8. **Externalize thresholds** to `application.properties`
