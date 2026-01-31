# Monitor Subproject

## Overview

Spring Boot application demonstrating monitoring and observability using Actuator, Prometheus, and Grafana.

- **Java**: 21
- **Spring Boot**: 3.5.9
- **Database**: H2 (in-memory)

## Project Structure

```
monitor/
├── src/main/java/com/example/monitor/
│   ├── SpringMonitorApplication.java       # Entry point
│   ├── config/
│   │   ├── MetricsConfig.java              # Custom metrics (Counter, Timer)
│   │   └── DataSeederConfig.java           # Sample data seeder
│   ├── controller/RootController.java      # REST endpoints
│   ├── entity/Customer.java                # JPA entity
│   ├── health/
│   │   └── ExternalApiHealthIndicator.java # Custom health check
│   └── repository/CustomerRepository.java
├── src/main/resources/application.yml      # Configuration
├── src/gatling/java/.../CustomerSimulation.java  # Load testing
├── docker/
│   ├── docker-compose.yml                  # Prometheus + Grafana stack
│   ├── prometheus.yml                      # Scrape configuration
│   ├── prometheus-alerts.yml               # Alert rules
│   ├── grafana-dashboard.json              # Pre-built dashboard
│   └── grafana-provisioning/               # Auto-provisioning configs
└── build.gradle
```

## Key Dependencies

- `spring-boot-starter-actuator` - Monitoring endpoints
- `spring-boot-starter-data-jpa` - Data persistence
- `micrometer-registry-prometheus` - Prometheus metrics export
- `micrometer-tracing-bridge-brave` - Distributed tracing
- `zipkin-reporter-brave` - Zipkin integration
- `h2` - Embedded database
- `lombok` - Boilerplate reduction
- `io.gatling.gradle` - Load testing

## API Endpoints

| Endpoint | Purpose |
|----------|---------|
| `GET /ping` | Health check (minimal latency) |
| `GET /customers` | List all customers (increments `customer.access` counter) |
| `GET /customers/transform` | Slow endpoint (0-5s delay), timed by `customer.transform` |

## Custom Metrics

- `customer.access` - Counter for customer list accesses
- `customer.transform` - Timer for transform operation duration

## Actuator Endpoints

- `/actuator/health` - Health status with details
- `/actuator/health/liveness` - Liveness probe
- `/actuator/health/readiness` - Readiness probe (includes db, externalApi)
- `/actuator/metrics` - Available metrics
- `/actuator/prometheus` - Prometheus-formatted metrics

## Configuration Highlights

```yaml
# Health groups
management:
  endpoint:
    health:
      group:
        liveness:
          include: livenessState
        readiness:
          include: readinessState, db, externalApi

# Metrics distribution
management:
  metrics:
    distribution:
      percentiles[http.server.requests]: 0.5, 0.7, 0.95, 0.99
      slo[http.server.requests]: 10ms, 100ms

# Tracing
management:
  tracing:
    sampling:
      probability: 1.0

# HikariCP pool sizing
spring:
  datasource:
    hikari:
      maximum-pool-size: 10
      minimum-idle: 5
      connection-timeout: 30000
      leak-detection-threshold: 2000
```

## Running the Application

```bash
# Start the app
./gradlew :monitor:bootRun

# Start monitoring stack
cd monitor/docker && docker-compose up -d
```

## Access Points

- **Application**: http://localhost:8080
- **Prometheus**: http://localhost:9090
- **Grafana**: http://localhost:3000 (admin/admin)

## Grafana Dashboard

Auto-provisioned on startup with panels for:
- Request rate by endpoint
- Response time percentiles (p50, p95, p99)
- Error rate by endpoint
- JVM heap usage
- HikariCP connection pool stats
- Active threads
- Custom business metrics

## Prometheus Alerts

Pre-configured alerts in `prometheus-alerts.yml`:
- `HighResponseTime` - p95 > 1s for 1 minute
- `HighErrorRate` - Error rate > 10% for 2 minutes
- `HighMemoryUsage` - Heap > 90% for 5 minutes
- `DatabaseConnectionPoolExhausted` - Pool > 90% for 2 minutes
- `ApplicationDown` - App unreachable for 1 minute

## Load Testing

```bash
./gradlew :monitor:gatlingRun
```

Runs CustomerSimulation: ramps 5-100 users over 160 seconds hitting `/customers` and `/customers/transform`.

## Architecture

```
Controller Layer (@RestController)
       ↓
Repository Layer (JpaRepository)
       ↓
Entity Layer (@Entity)
```

Uses constructor injection via Lombok `@RequiredArgsConstructor`.
