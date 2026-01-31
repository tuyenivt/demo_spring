# demo_spring_monitor

Spring Boot application demonstrating monitoring and observability using Actuator, Prometheus, and Grafana.

## Quick Start

```bash
# Start the application
./gradlew :monitor:bootRun

# Start monitoring stack (Prometheus + Grafana)
docker compose -f docker/docker-compose.yml up -d
```

## Access Points

| Service     | URL                   | Credentials |
|-------------|-----------------------|-------------|
| Application | http://localhost:8080 | -           |
| Prometheus  | http://localhost:9090 | -           |
| Grafana     | http://localhost:3000 | admin/admin |

## Features

### Custom Business Metrics
- `customer.access` - Counter tracking customer list accesses
- `customer.transform` - Timer measuring transform operation duration

### Health Checks
- Built-in health indicators (db, diskSpace)
- Custom `externalApi` health indicator
- Health groups: liveness and readiness probes

### Alerting Rules
Pre-configured Prometheus alerts in `docker/prometheus-alerts.yml`:
- High response time (p95 > 1s)
- High error rate (> 10%)
- High memory usage (heap > 90%)
- Database connection pool exhaustion
- Application down

### Grafana Dashboard
Auto-provisioned dashboard with panels for:
- Request rate by endpoint
- Response time percentiles (p50, p95, p99)
- Error rate by endpoint
- JVM heap usage
- HikariCP connection pool stats
- Active threads
- Custom metrics

### Distributed Tracing
Integrated with Micrometer Tracing (Brave) for request correlation.

## API Endpoints

| Endpoint                   | Description                                    |
|----------------------------|------------------------------------------------|
| `GET /ping`                | Health check (minimal latency)                 |
| `GET /customers`           | List all customers (increments access counter) |
| `GET /customers/transform` | Slow endpoint (0-5s delay) for latency testing |

## Actuator Endpoints

- `/actuator/health` - Health status with details
- `/actuator/health/liveness` - Liveness probe
- `/actuator/health/readiness` - Readiness probe
- `/actuator/metrics` - Available metrics
- `/actuator/prometheus` - Prometheus-formatted metrics

## Load Testing

```bash
./gradlew :monitor:gatlingRun
```

Runs CustomerSimulation: ramps 5-100 users over 160 seconds.
