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
│   ├── SpringMonitorApplication.java    # Entry point
│   ├── controller/RootController.java   # REST endpoints
│   ├── entity/Customer.java             # JPA entity
│   └── repository/CustomerRepository.java
├── src/main/resources/application.yml   # Configuration
├── src/gatling/java/.../CustomerSimulation.java  # Load testing
├── docker/
│   ├── docker-compose.yml               # Prometheus + Grafana stack
│   └── prometheus.yml                   # Scrape configuration
└── build.gradle
```

## Key Dependencies

- `spring-boot-starter-actuator` - Monitoring endpoints
- `spring-boot-starter-data-jpa` - Data persistence
- `micrometer-registry-prometheus` - Prometheus metrics export
- `h2` - Embedded database
- `lombok` - Boilerplate reduction
- `io.gatling.gradle` - Load testing

## API Endpoints

| Endpoint | Purpose |
|----------|---------|
| `GET /ping` | Health check (minimal latency) |
| `GET /customers` | List all customers |
| `GET /customers/transform` | Slow endpoint (0-5s delay) for latency testing |

## Actuator Endpoints

- `/actuator/health` - Health status with details
- `/actuator/metrics` - Available metrics
- `/actuator/prometheus` - Prometheus-formatted metrics

## Configuration Highlights

```yaml
# Metrics configuration
management:
  metrics:
    distribution:
      percentiles[http.server.requests]: 0.5, 0.7, 0.95, 0.99
      slo[http.server.requests]: 10ms, 100ms

# Connection pool leak detection
spring.datasource.hikari.leak-detection-threshold: 2000
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
