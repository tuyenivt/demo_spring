# Rate Limiting Module

## Overview

A Spring Boot application demonstrating distributed rate limiting using Redis and the Bucket4j token bucket algorithm. Rate limits are applied declaratively via annotations with AOP interception.

## Tech Stack

- Spring Boot with AOP
- Redis (Lettuce client)
- Bucket4j (token bucket algorithm)
- Lombok

## Project Structure

```
rate-limiting/
├── src/main/java/com/example/ratelimiting/
│   ├── MainApplication.java              # Entry point
│   ├── controller/
│   │   └── HomeController.java           # REST endpoints
│   └── ratelimit/
│       ├── RateLimit.java                # Annotation definition
│       ├── RateLimitAspect.java          # AOP aspect
│       ├── RateLimitService.java         # Token bucket logic
│       ├── RedisBucketConfig.java        # Redis/Bucket4j config
│       └── UserContext.java              # User identification
└── src/main/resources/
    └── application.yml                   # Redis configuration
```

## Key Components

### RateLimit Annotation
Method-level annotation defining rate limit parameters:
- `limit`: Maximum requests allowed
- `durationSeconds`: Time window in seconds

### RateLimitAspect
AOP `@Before` advice intercepting annotated methods. Constructs Redis key: `rate-limit:user:{userId}:{method}` and delegates to RateLimitService. Returns HTTP 429 when limit exceeded.

### RateLimitService
Manages Bucket4j token buckets via Redis. Uses `refillIntervally` strategy - tokens refill completely after each interval.

### UserContext
Request-scoped bean extracting user ID from `X-USER-ID` header.

## Configuration

Redis connection in `application.yml`:
- Host: localhost:6379
- Pool: max-active=5, max-idle=5

## API Endpoints

| Endpoint | Rate Limit |
|----------|------------|
| GET /api/hello | None |
| GET /api/orders | 5 req/60s per user |

## Testing

```bash
# No rate limit
curl localhost:8080/api/hello

# Rate limited (requires X-USER-ID header)
curl localhost:8080/api/orders -H "X-USER-ID: user123"
```

## Dependencies

```gradle
implementation 'org.springframework.boot:spring-boot-starter-aop'
implementation 'org.springframework.boot:spring-boot-starter-data-redis'
implementation 'org.springframework.boot:spring-boot-starter-web'
implementation 'com.bucket4j:bucket4j_jdk17-lettuce:8.15.0'
```
