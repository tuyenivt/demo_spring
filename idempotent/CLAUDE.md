# Idempotent Module

## Overview

AOP-based idempotency library using Redis for duplicate request detection and response caching.

## Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                    HTTP Request                             │
│              (with Idempotent-Key header)                   │
└─────────────────────┬───────────────────────────────────────┘
                      │
                      ▼
┌─────────────────────────────────────────────────────────────┐
│                  IdempotentAspect                           │
│  ┌─────────────────────────────────────────────────────┐    │
│  │ @Around("@annotation(Idempotent)")                  │    │
│  │ @Before("@annotation(PreventRepeatedRequests)")     │    │
│  └─────────────────────────────────────────────────────┘    │
└─────────────────────┬───────────────────────────────────────┘
                      │
                      ▼
┌─────────────────────────────────────────────────────────────┐
│                      Redis                                  │
│  Key: {prefix}_{ip}_{path}_{clientKey}                      │
│  Value: "first-request" | cached result                     │
└─────────────────────────────────────────────────────────────┘
```

## Project Structure

```
idempotent/
├── src/main/java/com/example/idempotent/
│   ├── MainApplication.java
│   ├── config/
│   │   └── AppConfig.java              # App-level config values
│   └── idempotent/
│       ├── Idempotent.java             # Full caching annotation
│       ├── PreventRepeatedRequests.java # Simple blocking annotation
│       ├── IdempotentAspect.java       # Core AOP logic
│       ├── IdempotentConfig.java       # Idempotent settings
│       ├── IdempotentException.java    # Duplicate error
│       └── IdempotentRedisConfig.java  # Redis cache config
└── src/main/resources/
    └── application.yml
```

## Key Components

### Annotations

| Annotation | Purpose | Use Case |
|------------|---------|----------|
| `@Idempotent` | Caches full response, returns cached on duplicate | Payments, orders, critical operations |
| `@PreventRepeatedRequests` | Blocks duplicates, no result caching | Form submissions, button clicks |

### Cache Key Composition

**@Idempotent**: `{prefix}_{clientIP}_{requestPath}_{headerKey}`
**@PreventRepeatedRequests**: `{prefix}_{methodName}_{headerKey|args}`

### HTTP Headers

| Header | Purpose |
|--------|---------|
| `Idempotent-Key` | Client-provided unique request identifier |
| `Idempotent-Replay: true` | Force retrieval of cached result |

## Configuration

```yaml
app:
  idempotent:
    timeout-minutes: 10         # Duplicate detection window
    result-expire-minutes: 1440 # Result cache TTL (24h)
    cache-store-key: my-idempotent
    client-header-key: Idempotent-Key
    client-header-replay: Idempotent-Replay
```

## Dependencies

- Spring Boot AOP
- Spring Data Redis
- Spring Boot Web
- Lombok

## Request Flow

1. **New request**: Execute method → cache result → return
2. **Duplicate (in progress)**: Return 409 Conflict with TTL info
3. **Duplicate (completed)**: Return cached result
4. **Replay header**: Bypass check, return cached result

## Usage Example

```java
@RestController
public class PaymentController {

    @PostMapping("/payments")
    @Idempotent  // Full response caching
    public Payment createPayment(@RequestBody PaymentRequest req) {
        return paymentService.process(req);
    }

    @PostMapping("/subscribe")
    @PreventRepeatedRequests  // Simple duplicate blocking
    public void subscribe(@RequestBody SubscribeRequest req) {
        subscriptionService.subscribe(req);
    }
}
```

## Error Handling

`IdempotentException` is thrown on duplicates. Handle globally:

```java
@ExceptionHandler(IdempotentException.class)
public ResponseEntity<ErrorResponse> handleIdempotent(IdempotentException ex) {
    return ResponseEntity.status(HttpStatus.CONFLICT)
        .body(new ErrorResponse("Duplicate request", ex.getMessage()));
}
```

## Build & Run

```bash
# Requires Redis running on localhost:6379
./gradlew :idempotent:bootRun
```
