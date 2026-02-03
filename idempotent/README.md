# Idempotent Module

AOP-based idempotency library using Redis for duplicate request detection and response caching.

## Features

- **@Idempotent**: Full response caching with automatic replay on duplicate requests
- **@PreventRepeatedRequests**: Simple duplicate blocking without response caching
- Redis-backed distributed idempotency
- Configurable TTL for duplicate detection and result caching
- Global exception handling with structured error responses

## Quick Start

### Prerequisites

- Java 21+
- Redis server running on localhost:6379

### Run the Application

```bash
./gradlew :idempotent:bootRun
```

### Test Idempotency

```bash
# First request - processes payment
curl -X POST http://localhost:8080/api/demo/payments \
  -H "Content-Type: application/json" \
  -H "Idempotent-Key: payment-123" \
  -d '{"amount": 100.00, "currency": "USD"}'

# Duplicate request - returns cached result (same transaction ID)
curl -X POST http://localhost:8080/api/demo/payments \
  -H "Content-Type: application/json" \
  -H "Idempotent-Key: payment-123" \
  -d '{"amount": 100.00, "currency": "USD"}'
```

## Usage

### @Idempotent Annotation

Use for critical operations where you need to cache and replay the response:

```java
@PostMapping("/payments")
@Idempotent
public Payment processPayment(@RequestBody PaymentRequest request) {
    return paymentService.process(request);
}
```

### @PreventRepeatedRequests Annotation

Use for simple duplicate prevention without response caching:

```java
@PostMapping("/subscribe")
@PreventRepeatedRequests
public void subscribe(@RequestBody SubscribeRequest request) {
    subscriptionService.subscribe(request);
}
```

## HTTP Headers

| Header | Description |
|--------|-------------|
| `Idempotent-Key` | Required. Unique identifier for the request |
| `Idempotent-Replay` | Optional. Set to `true` to force re-execution |

## Configuration

```yaml
app:
  idempotent:
    timeout-minutes: 10         # Duplicate detection window
    result-expire-minutes: 1440 # Result cache TTL (24 hours)
```

## Demo Endpoints

| Endpoint | Annotation | Use Case |
|----------|------------|----------|
| `POST /api/demo/payments` | @Idempotent | Payment processing |
| `POST /api/demo/orders` | @Idempotent | Order creation |
| `POST /api/demo/subscriptions` | @PreventRepeatedRequests | Newsletter signup |

## Testing

Run integration tests (requires Docker):

```bash
./gradlew :idempotent:test
```

## Error Response

Duplicate requests return HTTP 409 Conflict:

```json
{
  "code": "DUPLICATE_REQUEST",
  "message": "Request already processed or in progress",
  "detail": "...",
  "timestamp": "2024-01-15T10:30:00Z"
}
```
