# Idempotent Module

## Overview

AOP-based idempotency library using Redis for duplicate request detection and response caching. Includes demo endpoints showcasing both `@Idempotent` and `@PreventRepeatedRequests` annotations.

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
│   ├── controller/
│   │   ├── PaymentDemoController.java  # @Idempotent demo
│   │   ├── OrderDemoController.java    # @Idempotent demo
│   │   ├── SubscriptionDemoController.java # @PreventRepeatedRequests demo
│   │   └── IdempotentExceptionHandler.java # Global exception handler
│   ├── dto/
│   │   ├── PaymentRequest.java
│   │   ├── PaymentResponse.java
│   │   ├── OrderRequest.java
│   │   ├── OrderResponse.java
│   │   ├── OrderItem.java
│   │   ├── SubscribeRequest.java
│   │   └── ErrorResponse.java
│   └── idempotent/
│       ├── Idempotent.java             # Full caching annotation
│       ├── PreventRepeatedRequests.java # Simple blocking annotation
│       ├── IdempotentAspect.java       # Core AOP logic
│       ├── IdempotentConfig.java       # Idempotent settings
│       ├── IdempotentException.java    # Duplicate error
│       └── IdempotentRedisConfig.java  # Redis cache config
├── src/test/java/com/example/idempotent/
│   └── IdempotentIntegrationTest.java  # Testcontainers integration tests
└── src/main/resources/
    └── application.yml
```

## Key Components

### Annotations

| Annotation                 | Purpose                                           | Use Case                              | Configuration                         |
|----------------------------|---------------------------------------------------|---------------------------------------|---------------------------------------|
| `@Idempotent`              | Caches full response, returns cached on duplicate | Payments, orders, critical operations | `timeout`, `timeUnit`, `resultExpire` |
| `@PreventRepeatedRequests` | Blocks duplicates, no result caching              | Form submissions, button clicks       | `timeout`, `timeUnit`                 |

Both annotations support per-endpoint configuration. Set `timeout = -1` to use global config.

### Cache Key Composition

**@Idempotent**: `{prefix}_{clientIP}_{requestPath}_{headerKey}`
**@PreventRepeatedRequests**: `{prefix}_{methodName}_{headerKey|args}`

### HTTP Headers

| Header                    | Required | Purpose                                                                       |
|---------------------------|----------|-------------------------------------------------------------------------------|
| `Idempotent-Key`          | **Yes**  | Client-provided unique request identifier (validated, returns 400 if missing) |
| `Idempotent-Replay: true` | No       | Force re-execution and cache update                                           |

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
- Spring Boot Validation (Bean Validation)
- Lombok
- Testcontainers (testing)
- Mockito (unit testing)

## Request Flow

1. **New request**: Atomic lock acquisition → execute method → cache result → return
2. **Duplicate (in progress)**: Return 409 Conflict with TTL info
3. **Duplicate (completed)**: Return cached result
4. **Replay header**: Delete cache, execute method, update cache
5. **Method failure**: Delete cache key to allow retries

## Security & Reliability Features

- **Atomic Redis operations**: Uses `SETNX` to prevent race conditions on concurrent duplicate requests
- **Required idempotent key**: Validates `Idempotent-Key` header presence (returns 400 Bad Request if missing)
- **Cleanup on failure**: Deletes cache keys when method execution fails to allow retries
- **Per-endpoint configuration**: Override global timeout/expiry settings per annotation
- **Request validation**: All DTOs validated with Bean Validation annotations

## Demo Endpoints

| Endpoint                     | Method | Annotation                 | Description                                      |
|------------------------------|--------|----------------------------|--------------------------------------------------|
| `/api/demo/payments`         | POST   | `@Idempotent`              | Payment processing with full response caching    |
| `/api/demo/orders`           | POST   | `@Idempotent`              | Order creation with full response caching        |
| `/api/demo/orders/{orderId}` | DELETE | `@Idempotent`              | Idempotent order cancellation                    |
| `/api/demo/subscriptions`    | POST   | `@PreventRepeatedRequests` | Newsletter signup with simple duplicate blocking |

### Testing Demo Endpoints

```bash
# Payment: First request processes payment
curl -X POST http://localhost:8080/api/demo/payments \
  -H "Content-Type: application/json" \
  -H "Idempotent-Key: payment-123" \
  -d '{"amount": 100.00, "currency": "USD", "description": "Test payment"}'

# Payment: Duplicate request returns cached result (no double charge)
curl -X POST http://localhost:8080/api/demo/payments \
  -H "Content-Type: application/json" \
  -H "Idempotent-Key: payment-123" \
  -d '{"amount": 100.00, "currency": "USD", "description": "Test payment"}'

# Order: Create order with idempotency
curl -X POST http://localhost:8080/api/demo/orders \
  -H "Content-Type: application/json" \
  -H "Idempotent-Key: order-456" \
  -d '{"items": [{"productId": "PROD-001", "productName": "Widget", "quantity": 2, "price": 50.00}], "shippingAddress": "123 Main St"}'

# Order: Cancel order (idempotent DELETE)
curl -X DELETE http://localhost:8080/api/demo/orders/order-123 \
  -H "Idempotent-Key: cancel-456"

# Subscription: First request succeeds
curl -X POST http://localhost:8080/api/demo/subscriptions \
  -H "Content-Type: application/json" \
  -H "Idempotent-Key: sub-789" \
  -d '{"email": "test@example.com", "name": "Test User"}'

# Subscription: Duplicate returns 409 Conflict
curl -X POST http://localhost:8080/api/demo/subscriptions \
  -H "Content-Type: application/json" \
  -H "Idempotent-Key: sub-789" \
  -d '{"email": "test@example.com", "name": "Test User"}'

# Missing Idempotent-Key header returns 400 Bad Request
curl -X POST http://localhost:8080/api/demo/payments \
  -H "Content-Type: application/json" \
  -d '{"amount": 100.00, "currency": "USD"}'

# Invalid request data returns 400 Bad Request (validation)
curl -X POST http://localhost:8080/api/demo/payments \
  -H "Content-Type: application/json" \
  -H "Idempotent-Key: payment-999" \
  -d '{"amount": -100.00, "currency": ""}'
```

## Error Handling

Duplicate requests return HTTP 409 Conflict:

```json
{
  "code": "DUPLICATE_REQUEST",
  "message": "Request already processed or in progress",
  "detail": "Repeated requests, previous request expired in 10 minutes",
  "timestamp": "2024-01-15T10:30:00Z"
}
```

## Build & Run

```bash
# Requires Redis running on localhost:6379
./gradlew :idempotent:bootRun

# Run integration tests (requires Docker for Testcontainers)
./gradlew :idempotent:test
```
