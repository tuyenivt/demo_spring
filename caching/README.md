# Spring Boot Redis Caching Demo

A comprehensive demonstration of Redis caching patterns in Spring Boot, including cache-aside strategy, TTL configuration, pub/sub messaging, and REST API endpoints.

## Features

- **Spring Cache Abstraction** - Declarative caching with `@Cacheable`, `@CachePut`, `@CacheEvict`
- **Cache-Aside Pattern** - Automatic fallback to database when cache fails
- **TTL Configuration** - Per-cache expiration times with JSON serialization
- **Connection Pooling** - Lettuce pool for efficient Redis connections
- **Cache Statistics** - Monitor cache performance via Actuator
- **Cache Warming** - Pre-populate cache on application startup
- **Manual Cache Operations** - REST API for cache management
- **Pub/Sub Messaging** - Redis publish/subscribe demonstration
- **Activity Tracking** - Redis list operations (FIFO queue)

## Quick Start

### 1. Start Infrastructure

```bash
# Start Redis
docker run -d --name redis -p 6379:6379 redis:8.4-alpine

# Start MySQL
docker run -d --name mysql -p 3306:3306 -e MYSQL_ROOT_PASSWORD=root -e MYSQL_DATABASE=demodb mysql:8.4
```

### 2. Run Application

```bash
./gradlew :caching:bootRun
```

## API Endpoints

### Product Operations

```bash
# Create a product
curl -X POST http://localhost:8080/api/products -H "Content-Type: application/json" -d '{"productName":"Widget","category":"PRODUCT","inStock":100,"price":29.99,"dateOfManufacture":"2026-01-02T09:59:01","vendor":"ABC"}'

# Get product by ID (cache miss, then cache hit)
curl http://localhost:8080/api/products/1
curl http://localhost:8080/api/products/1  # faster - from cache

# Search products by name
curl "http://localhost:8080/api/products/search?name=Widget"

# Update product
curl -X PUT http://localhost:8080/api/products/1 -H "Content-Type: application/json" -d '{"productName":"Widget Pro","inStock":150,"price":39.99}'
```

### Cache Management

```bash
# List all cache names
curl http://localhost:8080/api/cache/names

# Clear a specific cache
curl -X DELETE http://localhost:8080/api/cache/product

# Evict a specific key
curl -X DELETE http://localhost:8080/api/cache/product/1
```

### Pub/Sub Messaging

```bash
# Publish a message (default channel: my-channel)
curl -X POST "http://localhost:8080/api/messages/publish" -H "Content-Type: text/plain" -d "Hello Redis!"

# Publish to custom channel
curl -X POST "http://localhost:8080/api/messages/publish?channel=notifications" -H "Content-Type: text/plain" -d "New notification"
```

### User Activity Tracking

```bash
# Add user activity
curl -X POST http://localhost:8080/api/users/user123/activities -H "Content-Type: text/plain" -d "Logged in"

# Get oldest activity (FIFO)
curl http://localhost:8080/api/users/user123/activities/oldest
```

### Actuator Endpoints

```bash
# Health check
curl http://localhost:8080/actuator/health

# Cache statistics
curl http://localhost:8080/actuator/caches

# Application metrics
curl http://localhost:8080/actuator/metrics
```

## Cache Configuration

| Cache          | TTL        | Purpose               |
|----------------|------------|-----------------------|
| `product`      | 1 hour     | Single product lookup |
| `product_list` | 15 minutes | Product name search   |
| default        | 30 minutes | Other caches          |

## Architecture

```
┌─────────────────┐     ┌─────────────────┐     ┌─────────────────┐
│   Controller    │────▶│     Service     │────▶│   Repository    │
└─────────────────┘     └─────────────────┘     └─────────────────┘
                               │                        │
                               │                        │
                        ┌──────▼──────┐          ┌──────▼──────┐
                        │    Redis    │          │    MySQL    │
                        │   (Cache)   │          │    (DB)     │
                        └─────────────┘          └─────────────┘
```

### Cache-Aside Flow

1. Request comes in for a product
2. Service tries to get from cache via `ProductCacheRepository`
3. On cache hit: return cached data immediately
4. On cache miss: fetch from DB, cache it, return data
5. On cache error: fallback to `ProductRepository` (direct DB access)

## Connection Pooling

Lettuce connection pool is configured for optimal Redis performance:

```yaml
spring:
  data:
    redis:
      lettuce:
        pool:
          enabled: true
          max-active: 8
          max-idle: 8
          min-idle: 2
          max-wait: -1ms
```

## Monitoring

Access cache statistics via Spring Actuator:

```bash
curl http://localhost:8080/actuator/caches
```

Response includes hit/miss counts, eviction statistics, and cache size information.
