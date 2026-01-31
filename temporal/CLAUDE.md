# Temporal Subproject

## Overview

Spring Boot application demonstrating **Temporal.io** workflow orchestration for order processing. Features graceful shutdown, child workflows, activity retries, and production-ready patterns.

## Tech Stack

- Java 21+ with Virtual Threads
- Spring Boot with Actuator
- Temporal SDK 1.32.1
- Gradle build

## Project Structure

```
temporal/
├── src/main/java/com/example/temporal/
│   ├── MainApplication.java           # Entry point
│   ├── controller/
│   │   └── OrderController.java       # REST API endpoints
│   ├── config/
│   │   ├── TemporalConfig.java        # Temporal client & worker setup
│   │   └── TemporalProperties.java    # Configuration properties
│   ├── workflows/
│   │   ├── OrderWorkflow.java         # Main workflow interface
│   │   ├── PaymentChildWorkflow.java  # Payment child workflow
│   │   ├── InventoryChildWorkflow.java
│   │   └── impl/                       # Workflow implementations
│   ├── activities/
│   │   ├── OrderActivities.java       # Activity interfaces
│   │   ├── PaymentActivities.java
│   │   └── impl/                       # Activity implementations
│   ├── dto/                            # Request/Response DTOs
│   └── exception/                      # Custom exceptions
└── src/main/resources/
    └── application.yml                 # Configuration
```

## API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/orders` | Start order workflow async (recommended) |
| POST | `/api/orders/sync` | Start workflow sync (demo only) |
| GET | `/api/orders/{orderId}/status` | Get workflow status |

## Configuration

```yaml
temporal:
  namespace: default
  task-queue: ORDER_PROCESSING_QUEUE
  target: localhost:7233
  ui: http://localhost:8233
  worker:
    shutdown-timeout-seconds: 30
```

## Workflow Architecture

```
OrderWorkflow (Main)
├── validateOrder() [Activity - 3 retries]
├── PaymentChildWorkflow
│   ├── authorizePayment() [Activity - 5 retries]
│   └── capturePayment() [Activity - 5 retries]
├── InventoryChildWorkflow
│   ├── checkInventory() [Activity - 3 retries]
│   └── reserveInventory() [Activity - 3 retries]
└── sendNotification() [Activity]
```

## Key Patterns

1. **Graceful Shutdown**: Sticky task queue drain for clean termination
2. **Activity Retries**: Exponential backoff (1s initial, 2.0 coefficient, 10s max)
3. **Child Workflows**: Payment and inventory as separate reusable workflows
4. **Workflow/Activity Separation**: Deterministic orchestration vs I/O operations

## Running Locally

```bash
# Start Temporal server
docker run -d --name temporal -p 7233:7233 -p 8233:8233 \
  temporalio/temporal:1.5.1 server start-dev

# Run application
./gradlew :temporal:bootRun

# Test workflow
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d '{"customerId":"CUST-123","amount":9999,"description":"Widget"}'
```

## Development Notes

- Activities simulate transient failures (10-20% rate) for retry demo
- Virtual threads enabled for improved concurrency
- Temporal UI at http://localhost:8233 for workflow inspection
