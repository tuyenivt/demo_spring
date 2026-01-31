# Caching Module

## Overview

Spring Boot caching demo using Redis as the cache backend. Demonstrates Spring Cache abstraction, direct Redis operations, and the cache-aside pattern with graceful database fallback.

## Tech Stack

- Java 21+
- Spring Boot
- Spring Data JPA (MySQL)
- Spring Data Redis
- Lombok

## Project Structure

```
caching/
├── src/main/java/com/example/caching/
│   ├── MainApplication.java          # Entry point
│   ├── config/
│   │   └── RedisConfig.java          # Redis pub/sub configuration
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

### Cache Names

| Cache | Purpose | Key Pattern |
|-------|---------|-------------|
| `product` | Single product lookup | `{id}` or `{id}_{dateOfManufacture}` |
| `product_list` | Product name search | `myPrefix_{productName}` |

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
    type: redis           # Use Redis as cache manager
  redis:
    host: localhost
    port: 6379
    database: 0
    timeout: 60000
```

### Dependencies (build.gradle)

```gradle
implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
implementation 'org.springframework.boot:spring-boot-starter-data-redis'
runtimeOnly 'com.mysql:mysql-connector-j'
```

## Redis Features Demonstrated

1. **Spring Cache Abstraction** - Method-level caching via annotations
2. **Pub/Sub Messaging** - `RedisPublisher` and `RedisSubscriber` on channel `my-channel`
3. **List Operations** - Activity queue in `UserService` (FIFO pattern)

## Data Flow

```
Request → ProductService
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

- MySQL database `demo` with `product` table
- Redis server on `localhost:6379`

## Running

```bash
./gradlew :caching:bootRun
```
