# AOP Subproject

## Overview
Production-ready Spring AOP demonstration showcasing annotation-driven aspects with clear separation of concerns.

## Architecture

The aspect structure follows a **pragmatic, annotation-driven pattern** suitable for production use:

```
Production Aspects (opt-in via annotations):
├── ExecutionLoggingAspect       - @ExecutionLogging for detailed method logging
├── PerformanceMonitoringAspect  - @MonitorPerformance for slow method detection
├── RetryAspect                  - @Retryable for automatic retry
├── CacheAspect                  - @SimpleCache for method-level caching
├── AuthorizationAspect          - @RequiresRole for RBAC
└── AuditAspect                  - @Audited for audit trails

Layer-Specific Aspects (automatic):
└── ControllerLoggingAspect      - Auto-logs @RestController with correlation IDs

Educational Aspects (demonstrations only):
└── DemoAspect       - Shows all 5 advice types + pointcut composition
```

## Key Design Principles

1. **Annotation-Driven**: Prefer explicit `@Annotation` opt-in over blanket execution pointcuts
2. **Single Responsibility**: Each aspect has ONE clear purpose
3. **Production-Ready**: Configurable, testable, and maintainable
4. **Educational Separation**: Demo code isolated in DemoAspect
5. **Package by Feature**: Related files grouped in subpackages (audit/, auth/, cache/, retry/)

## Project Structure
```
aop/
├── build.gradle
└── src/main/java/com/example/aop/
    ├── MainApplication.java
    ├── aspect/
    │   ├── audit/                             # Audit concern
    │   │   ├── AuditAspect.java
    │   │   ├── Audited.java
    │   │   └── AuditLog.java
    │   ├── auth/                              # Authorization concern
    │   │   ├── AccessDeniedException.java
    │   │   ├── AuthorizationAspect.java
    │   │   ├── RequiresRole.java
    │   │   └── SecurityContext.java
    │   ├── cache/                             # Caching concern
    │   │   ├── CacheAspect.java
    │   │   └── SimpleCache.java
    │   ├── retry/                             # Retry concern
    │   │   ├── RetryAspect.java
    │   │   └── Retryable.java
    │   ├── ControllerLoggingAspect.java       # @RestController logging (@Order 1)
    │   ├── DemoAspect.java                    # Educational demos (@Order 10)
    │   ├── ExecutionLogging.java              # Execution logging annotation
    │   ├── ExecutionLoggingAspect.java        # @ExecutionLogging handler (@Order 2)
    │   ├── MonitorPerformance.java            # Performance monitoring annotation
    │   └── PerformanceMonitoringAspect.java   # @MonitorPerformance handler (@Order 3)
    ├── dao/
    │   └── AccountDao.java                    # @Repository
    ├── entity/
    │   └── Account.java                       # Domain model
    └── service/
        └── AccountService.java                # @Service with @ExecutionLogging + @MonitorPerformance
```

## Aspect Ordering

Aspects execute in `@Order` sequence (lower = higher priority):

| Order | Aspect                          | Purpose                           |
|-------|---------------------------------|-----------------------------------|
| 0     | `RetryAspect`                   | Outermost retry wrapper           |
| 1     | `ControllerLoggingAspect`       | Correlation ID for @RestController|
| 2     | `ExecutionLoggingAspect`        | @ExecutionLogging detailed logs   |
| 3     | `PerformanceMonitoringAspect`   | @MonitorPerformance slow detection|
| 10    | `DemoAspect`        | Educational demos (low priority)  |

## Core Aspects

### ExecutionLoggingAspect (@Order 2)
**Purpose**: Detailed execution logging for specific methods via `@ExecutionLogging` annotation.

**Features**:
- Method signature + arguments logging
- Execution time tracking
- Return value logging (truncated if > 100 chars)
- Exception logging with timing

**Usage**:
```java
@ExecutionLogging
public void serve(int factor) throws InterruptedException {
    Thread.sleep(factor * 1000L);
}
```

**Output**:
```
INFO  Executing: AccountService.serve(..) with args: [1]
INFO  Completed: AccountService.serve(..) in 1002ms with result: null
```

### PerformanceMonitoringAspect (@Order 3)
**Purpose**: Detect slow methods via `@MonitorPerformance` annotation.

**Features**:
- Configurable threshold (method-level or global)
- Warns on slow execution
- Debug logs for normal execution

**Usage**:
```java
@MonitorPerformance(thresholdMs = 500)  // Method-specific threshold
public void serve(int factor) throws InterruptedException {
    Thread.sleep(factor * 1000L);
}

// Or use global threshold from config
@MonitorPerformance
public void anotherMethod() { ... }
```

**Configuration** (application.properties):
```properties
aop.performance.slow-threshold-ms=1000
```

**Output**:
```
WARN  SLOW METHOD: AccountService.serve(..) took 1002ms (threshold: 500ms)
```

### ControllerLoggingAspect (@Order 1)
**Purpose**: Automatic logging for all `@RestController` methods with correlation IDs for distributed tracing.

**Features**:
- Unique correlation ID per request (via MDC)
- Entry/exit logging
- Request duration tracking
- Exception logging

**Target**: All methods in `@RestController` classes (no annotation needed)

**Output**:
```
INFO  [a1b2c3d4] Entering: AccountController.getAccount(..) with args: [123]
INFO  [a1b2c3d4] Exiting: AccountController.getAccount(..) in 45ms with result: Account(id=123)
```

**MDC Integration**: Configure your logging pattern to include `%X{correlationId}`.

### DemoAspect (@Order 10)
**Purpose**: Educational demonstration of all 5 AspectJ advice types and pointcut composition.

**Demonstrates**:
- `@Before` - Pre-execution logging (AccountDao.add)
- `@AfterReturning` - Return value access (AccountDao.find)
- `@AfterThrowing` - Exception handling (AccountDao.delete)
- `@After` - Finally-style cleanup (find/delete)
- `@Around` - Full control (findOrExceptionIfNotFound)
- Pointcut composition: `&&` (AND), `!` (NOT)

**Note**: All logs prefixed with `[DEMO ...]` to distinguish from production aspects.

## Supporting Aspects

### RetryAspect (@Order 0)
Automatic retry with `@Retryable(maxAttempts = 3, retryOn = RuntimeException.class)`.

### CacheAspect
Method-level caching with `@SimpleCache` backed by ConcurrentHashMap.

### AuthorizationAspect
Role-based access control with `@RequiresRole("ADMIN")` and ThreadLocal SecurityContext.

### AuditAspect
Audit trail with `@Audited(action = "DELETE", entity = "Account")`.

## Annotations

| Annotation           | Target       | Purpose                                    |
|----------------------|--------------|--------------------------------------------|
| `@ExecutionLogging`  | Method       | Detailed execution logging with timing     |
| `@MonitorPerformance`| Method, Type | Detect slow methods (opt-in)               |
| `@Audited`           | Method       | Record audit trail                         |
| `@Retryable`         | Method       | Retry on failure                           |
| `@SimpleCache`       | Method       | Method-level caching                       |
| `@RequiresRole`      | Method       | Role-based authorization                   |

## Key Concepts

### Self-Invocation Limitation
Spring AOP is proxy-based. Internal method calls bypass the proxy:

```java
@Service
public class AccountService {
    public void processBatch(int factor) {
        serve(factor); // Bypasses @ExecutionLogging aspect!
    }

    @ExecutionLogging
    public void serve(int factor) { ... }
}
```

**Workarounds**: Inject self, use `AopContext.currentProxy()`, or move method to separate bean.

### Pointcut Composition
```java
// AND: @Service methods with @ExecutionLogging
@Pointcut("@within(Service) && @annotation(ExecutionLogging)")

// NOT: DAO methods excluding entity package
@Pointcut("execution(* com.example.aop.dao..*(..)) && !execution(* com.example.aop.entity..*(..))")
```

## Build & Run
```bash
./gradlew :aop:test
./gradlew :aop:bootRun
```

## Dependencies
- `spring-boot-starter-aop` — Core AOP support
- Lombok — Annotation processing
- JUnit 5 + OutputCaptureExtension — Testing with log assertion

## Migration from Old Structure

**Before** (tightly coupled, overlapping concerns):
```
CustomAspect - Mixed @ExecutionLogging + hardcoded DAO demos
LoggingAspect - @RestController auto-logging
PerformanceAspect - Blanket @Service monitoring
```

**After** (clean separation):
```
ExecutionLoggingAspect - Pure @ExecutionLogging handler
PerformanceMonitoringAspect - Opt-in @MonitorPerformance
ControllerLoggingAspect - @RestController (unchanged)
DemoAspect - Educational demos isolated
```

**Benefits**:
- Clear single responsibility per aspect
- Easier to enable/disable via annotations
- Better testability
- Production-ready patterns
- **Package by feature** - Each concern (audit, auth, cache, retry) isolated in its own subpackage
