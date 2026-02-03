# Caching Module

## Overview

Spring Boot caching demo using Redis as the cache backend. Demonstrates Spring Cache abstraction, direct Redis operations, cache-aside pattern with graceful database fallback, and REST API endpoints for testing.

## Tech Stack

- Java 21+
- Spring Boot
- Spring Web
- Spring Data JPA (MySQL)
- Spring Data Redis
- Spring Actuator
- Liquibase
- Lombok

## Project Structure

```
caching/
├── src/main/java/com/example/caching/
│   ├── MainApplication.java          # Entry point
│   ├── config/
│   │   ├── CacheConfig.java          # TTL, serialization, statistics
│   │   ├── CacheWarmer.java          # Cache warming on startup
│   │   └── RedisConfig.java          # Redis pub/sub configuration
│   ├── controller/
│   │   ├── ProductController.java    # Product CRUD endpoints
│   │   ├── CacheController.java      # Manual cache operations
│   │   ├── MessageController.java    # Pub/sub demo endpoint
│   │   └── UserActivityController.java # Activity tracking endpoint
│   ├── entity/
│   │   └── Product.java              # JPA entity (Serializable)
│   ├── enums/
│   │   └── Category.java             # Product category enum
│   ├── listener/
│   │   └── RedisSubscriber.java      # Pub/sub message listener
│   ├── repository/
│   │   ├── ProductRepository.java    # Direct database access
│   │   └── ProductCacheRepository.java # Cached database access
│   ├── sender/
│   │   └── RedisPublisher.java       # Pub/sub message publisher
│   └── service/
│       ├── ProductService.java       # Cache-aside with fallback
│       └── UserService.java          # Direct Redis list operations
└── src/main/resources/
    └── application.yml               # Configuration
```

## Key Components

### Cache Strategy

| Component | Pattern | Description |
|-----------|---------|-------------|
| `ProductCacheRepository` | Spring Cache abstraction | Declarative caching with `@Cacheable`, `@CachePut`, `@CacheEvict` |
| `ProductService` | Cache-aside | Try cache first, fallback to database on failure |
| `UserService` | Direct Redis | Low-level list operations for activity tracking |

### Cache Configuration

| Cache | TTL | Purpose |
|-------|-----|---------|
| `product` | 1 hour | Single product lookup |
| `product_list` | 15 minutes | Product name search |
| default | 30 minutes | Other caches |

### REST Endpoints

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/api/products/{id}` | GET | Get product by ID (cached) |
| `/api/products/search?name=` | GET | Search products by name (cached) |
| `/api/products` | POST | Create product |
| `/api/products/{id}` | PUT | Update product |
| `/api/cache/names` | GET | List all cache names |
| `/api/cache/{name}` | DELETE | Clear entire cache |
| `/api/cache/{name}/{key}` | DELETE | Evict specific key |
| `/api/messages/publish` | POST | Publish message to channel |
| `/api/users/{id}/activities` | POST | Add user activity |
| `/api/users/{id}/activities/oldest` | GET | Get oldest activity (FIFO) |

### Actuator Endpoints

| Endpoint | Description |
|----------|-------------|
| `/actuator/health` | Health check |
| `/actuator/caches` | Cache statistics |
| `/actuator/metrics` | Application metrics |

### Annotations Used

```java
// Cache on read (skip if null result)
@Cacheable(cacheNames = "product", unless = "#result == null", key = "#productId")

// Update cache on write (conditional)
@CachePut(cacheNames = "product", condition = "#entity.inStock gt 0", key = "...")

// Evict on modification
@CacheEvict(cacheNames = "product_list", key = "...")

// Combine multiple operations
@Caching(put = {...}, evict = {...})
```

## Configuration

### application.yml

```yaml
spring:
  cache:
    type: redis
  data:
    redis:
      host: localhost
      port: 6379
      lettuce:
        pool:
          enabled: true
          max-active: 8
          max-idle: 8
          min-idle: 2

management:
  endpoints:
    web:
      exposure:
        include: health,caches,metrics
```

### Dependencies (build.gradle)

```gradle
implementation 'org.springframework.boot:spring-boot-starter-web'
implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
implementation 'org.springframework.boot:spring-boot-starter-data-redis'
implementation 'org.springframework.boot:spring-boot-starter-actuator'
implementation 'org.apache.commons:commons-pool2'
implementation 'org.liquibase:liquibase-core'
runtimeOnly 'com.mysql:mysql-connector-j'
```

## Redis Features Demonstrated

1. **Spring Cache Abstraction** - Method-level caching via annotations
2. **TTL Configuration** - Per-cache expiration times
3. **JSON Serialization** - Human-readable cache values
4. **Connection Pooling** - Lettuce pool with commons-pool2
5. **Cache Statistics** - Via actuator endpoint
6. **Cache Warming** - Pre-populate cache on startup
7. **Manual Cache Operations** - Programmatic cache access via API
8. **Pub/Sub Messaging** - `RedisPublisher` and `RedisSubscriber` on channel `my-channel`
9. **List Operations** - Activity queue in `UserService` (FIFO pattern)

## Data Flow

```
Request → ProductController
            ↓
        ProductService
            ↓
        Try: ProductCacheRepository (with @Cacheable)
            ├─ Cache HIT → Return cached data
            ├─ Cache MISS → Query DB → Cache result → Return
            └─ Cache ERROR → Catch exception
                              ↓
                          ProductRepository (direct DB)
                              ↓
                          Return from database
```

## Requirements

- MySQL database `demodb` (schema managed by Liquibase)
- Redis server on `localhost:6379`

## Running

```bash
./gradlew :caching:bootRun
```
