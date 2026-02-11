# Rate Limiting Demo

Distributed rate limiting using Redis and Bucket4j. Supports user-based and IP-based identification, named configuration profiles, `INTERVALLY` and `GREEDY` refill strategies, class-level and repeatable annotations, stacked limits per endpoint, and standard rate-limit response headers.

## Quick Start

```bash
docker run -d --name redis -p 6379:6379 redis:8.4-alpine
./gradlew :rate-limiting:bootRun
```

## Configuration Profiles

Rate limits are defined as named profiles in `application.yml` instead of being hardcoded in annotations:

```yaml
rate-limiting:
  profiles:
    strict:   { limit: 5,   duration-seconds: 60,   strategy: INTERVALLY }
    standard: { limit: 20,  duration-seconds: 60,   strategy: GREEDY }
    relaxed:  { limit: 100, duration-seconds: 3600, strategy: INTERVALLY }
```

Profiles can be tuned per environment without code changes.

## API Endpoints

| Endpoint                             | Rate Limit         | Notes                    |
|--------------------------------------|--------------------|--------------------------|
| GET /api/hello                       | None               |                          |
| GET /api/orders                      | 5 req/60s          | `strict` profile         |
| GET /api/search                      | 20 req/60s         | `standard` / greedy      |
| GET /api/reports                     | 100 req/3600s      | `relaxed` profile        |
| GET /api/submit                      | 10/s **+** 100/min | Stacked limits           |
| GET /api/rate-limit/status?profile=X | —                  | Token status, no consume |

## Refill Strategies

| Strategy     | Behaviour                                                            |
|--------------|----------------------------------------------------------------------|
| `INTERVALLY` | All tokens refill at once when window expires (classic token bucket) |
| `GREEDY`     | Tokens added gradually, preventing boundary-burst spikes             |

## Stacked Limits

Apply multiple `@RateLimit` annotations to a single endpoint for layered protection:

```java
@RateLimits({
    @RateLimit(limit = 10, durationSeconds = 1, strategy = RefillStrategy.GREEDY),  // burst
    @RateLimit(limit = 100, durationSeconds = 60)                                   // sustained
})
@GetMapping("/submit")
public String submit() { ... }
```

## Class-Level Annotation

Apply `@RateLimit` to a controller class to limit all its endpoints under one bucket:

```java
@RateLimit(limit = 50, durationSeconds = 60)
@RestController
public class MyController { ... }
```

## API Tests

```bash
# No rate limit
curl localhost:8080/api/hello

# Rate limited with user ID (inspect headers)
curl -i localhost:8080/api/orders -H "X-USER-ID: user123"

# Rate limited via IP fallback
curl -i localhost:8080/api/orders

# Stacked limits demo
curl -i localhost:8080/api/submit -H "X-USER-ID: user123"

# Check token status without consuming
curl "localhost:8080/api/rate-limit/status?profile=strict" -H "X-USER-ID: user123"
```

## Response Headers

| Header                | Description                                 |
|-----------------------|---------------------------------------------|
| X-RateLimit-Limit     | Maximum requests allowed                    |
| X-RateLimit-Remaining | Remaining requests in current window        |
| X-RateLimit-Reset     | Unix timestamp when limit resets (accurate) |
| Retry-After           | Seconds to wait before retrying (on 429)    |

## Error Response (HTTP 429)

```json
{
  "status": 429,
  "error": "Too Many Requests",
  "message": "Rate limit exceeded. Please try again later.",
  "timestamp": 1234567890
}
```

## Resilience

When Redis is unavailable, the aspect **fails open** — requests are allowed through and a `WARN` log entry is emitted so operators are alerted without breaking the service.

## Running Tests

```bash
# All tests (unit + integration; Docker required for Testcontainers)
./gradlew :rate-limiting:test

# Unit tests only (no Docker)
./gradlew :rate-limiting:test --tests "com.example.ratelimiting.ratelimit.*"
```

Integration tests use Testcontainers to spin up a real Redis instance and include a concurrent-request test that verifies exactly `limit` requests succeed under parallel load.
