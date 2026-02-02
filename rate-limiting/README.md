# Rate Limiting Demo

Distributed rate limiting using Redis and Bucket4j token bucket algorithm. Supports user-based and IP-based rate limiting with standard rate limit headers.

## Quick Start

```bash
docker run -d --name redis -p 6379:6379 redis:8.4-alpine
```

## API Endpoints

| Endpoint         | Rate Limit   | Description       |
|------------------|--------------|-------------------|
| GET /api/hello   | None         | No rate limiting  |
| GET /api/orders  | 5 req/min    | Order operations  |
| GET /api/search  | 20 req/min   | Search operations |
| GET /api/reports | 100 req/hour | Report generation |

## API Tests

```bash
# No rate limit
curl localhost:8080/api/hello

# Rate limited with user ID
curl -i localhost:8080/api/orders -H "X-USER-ID: user123"

# Rate limited without user ID (IP-based)
curl -i localhost:8080/api/orders

# Search endpoint
curl -i localhost:8080/api/search -H "X-USER-ID: user123"

# Reports endpoint
curl -i localhost:8080/api/reports -H "X-USER-ID: user123"
```

## Response Headers

Rate limited endpoints return the following headers:

| Header                | Description                              |
|-----------------------|------------------------------------------|
| X-RateLimit-Limit     | Maximum requests allowed                 |
| X-RateLimit-Remaining | Remaining requests in current window     |
| X-RateLimit-Reset     | Unix timestamp when the limit resets     |
| Retry-After           | Seconds to wait before retrying (on 429) |

## Error Response

When rate limit is exceeded, the API returns HTTP 429 with:

```json
{
  "status": 429,
  "error": "Too Many Requests",
  "message": "Rate limit exceeded. Please try again later.",
  "timestamp": 1234567890
}
```

## Running Tests

```bash
./gradlew :rate-limiting:test
```

Tests use Testcontainers to spin up a real Redis instance.
