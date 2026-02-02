# Rate Limiting Module

## Overview

A Spring Boot application demonstrating distributed rate limiting using Redis and the Bucket4j token bucket algorithm. Rate limits are applied declaratively via annotations with AOP interception. Supports both user-based and IP-based rate limiting with standard rate limit headers.

## Tech Stack

- Spring Boot with AOP
- Redis (Lettuce client)
- Bucket4j (token bucket algorithm)
- Testcontainers (integration testing)
- Lombok

## Project Structure

```
rate-limiting/
├── src/main/java/com/example/ratelimiting/
│   ├── MainApplication.java              # Entry point
│   ├── controller/
│   │   └── HomeController.java           # REST endpoints
│   ├── exception/
│   │   └── GlobalExceptionHandler.java   # JSON error responses
│   └── ratelimit/
│       ├── RateLimit.java                # Annotation definition
│       ├── RateLimitAspect.java          # AOP aspect with headers
│       ├── RateLimitService.java         # Token bucket logic
│       ├── RedisBucketConfig.java        # Redis/Bucket4j config
│       └── UserContext.java              # User/IP identification
├── src/test/java/com/example/ratelimiting/
│   └── RateLimitIntegrationTest.java     # Testcontainers tests
└── src/main/resources/
    └── application.yml                   # Redis configuration
```

## Key Components

### RateLimit Annotation
Method-level annotation defining rate limit parameters:
- `limit`: Maximum requests allowed
- `durationSeconds`: Time window in seconds

### RateLimitAspect
AOP `@Before` advice intercepting annotated methods. Constructs Redis key: `rate-limit:{identifier}:{method}` and delegates to RateLimitService. Sets rate limit headers on every response. Returns HTTP 429 when limit exceeded.

### RateLimitService
Manages Bucket4j token buckets via Redis. Uses `refillIntervally` strategy - tokens refill completely after each interval. Returns `ConsumeResult` with consumption details for header population.

### UserContext
Request-scoped bean that identifies users by:
1. `X-USER-ID` header (if present)
2. Client IP address as fallback (supports `X-Forwarded-For` for proxied requests)

### GlobalExceptionHandler
Converts rate limit exceptions to structured JSON error responses with status, error type, message, and timestamp.

## Response Headers

All rate-limited endpoints return:
- `X-RateLimit-Limit`: Maximum requests allowed
- `X-RateLimit-Remaining`: Remaining requests in window
- `X-RateLimit-Reset`: Unix timestamp when limit resets
- `Retry-After`: Seconds to wait (only on 429)

## Configuration

Redis connection in `application.yml`:
- Host: localhost:6379
- Pool: max-active=5, max-idle=5

## API Endpoints

| Endpoint         | Rate Limit    |
|------------------|---------------|
| GET /api/hello   | None          |
| GET /api/orders  | 5 req/60s     |
| GET /api/search  | 20 req/60s    |
| GET /api/reports | 100 req/3600s |

## Testing

```bash
# No rate limit
curl localhost:8080/api/hello

# Rate limited with user ID (view headers)
curl -i localhost:8080/api/orders -H "X-USER-ID: user123"

# Rate limited with IP fallback
curl -i localhost:8080/api/orders
```

## Integration Tests

Run with Testcontainers (requires Docker):
```bash
./gradlew :rate-limiting:test
```

## Dependencies

```gradle
implementation 'org.springframework.boot:spring-boot-starter-aop'
implementation 'org.springframework.boot:spring-boot-starter-data-redis'
implementation 'org.springframework.boot:spring-boot-starter-web'
implementation 'com.bucket4j:bucket4j_jdk17-lettuce:8.16.0'
testImplementation 'org.springframework.boot:spring-boot-testcontainers'
testImplementation 'org.testcontainers:junit-jupiter'
```
