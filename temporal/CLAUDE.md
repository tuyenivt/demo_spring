# Temporal Subproject

## Overview

Spring Boot application demonstrating **Temporal.io** workflow orchestration for order processing. Features graceful shutdown, child workflows, saga pattern compensation, activity retries, signals/queries, search attributes, and production-ready patterns.

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
│   │   ├── OrderWorkflow.java         # Main workflow interface (signals, queries)
│   │   ├── PaymentChildWorkflow.java  # Payment child workflow
│   │   ├── InventoryChildWorkflow.java
│   │   └── impl/                       # Workflow implementations
│   ├── activities/
│   │   ├── OrderActivities.java       # Activity interfaces
│   │   ├── PaymentActivities.java     # With refund for saga compensation
│   │   └── impl/                       # Activity implementations (with heartbeat)
│   ├── dto/                            # Request/Response DTOs
│   └── exception/                      # Custom exceptions
├── src/test/java/com/example/temporal/
│   └── workflows/
│       └── OrderWorkflowTest.java     # Workflow unit tests
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
├── [Signal] cancelOrder(reason)        # Cancel running order
├── [Signal] updateShippingAddress()    # Update shipping address
├── [Query] getStatus()                 # Query current status
├── [Query] getShippingAddress()        # Query shipping address
│
├── validateOrder() [Activity - 3 retries]
│   └── Fraud check (v2 feature via versioning)
├── PaymentChildWorkflow
│   ├── authorizePayment() [Activity - 5 retries, heartbeat]
│   └── capturePayment() [Activity - 5 retries, heartbeat]
│   └── [Saga] refundPayment() on failure
├── InventoryChildWorkflow
│   ├── checkInventory() [Activity - 3 retries]
│   └── reserveInventory() [Activity - 3 retries]
│   └── [Saga] releaseInventory() on failure
└── sendNotification() [Activity]
```

## Key Patterns

### 1. Saga Pattern for Compensation
Compensations are registered after each successful step and executed in reverse order on failure:
- Payment refund if inventory reservation fails
- Inventory release if notification fails
- Ensures data consistency across distributed services

### 2. Workflow Signals
Async messages to modify running workflow state:
- `cancelOrder(reason)` - Cancel order during processing
- `updateShippingAddress(newAddress)` - Update shipping address

### 3. Workflow Queries
Read-only access to workflow state:
- `getStatus()` - Returns current OrderStatus enum
- `getShippingAddress()` - Returns current shipping address

### 4. Activity Heartbeat
Long-running activities report progress:
- Prevents Temporal from considering activity as failed
- Enables cancellation detection during operations
- Configured with 10-second heartbeat timeout

### 5. Search Attributes
Custom attributes for workflow discovery:
- `CustomerId` - Filter by customer
- `OrderAmount` - Filter by amount
- `OrderStatus` - Filter by status

Query example:
```bash
temporal workflow list --query "CustomerId='CUST-123' AND OrderStatus='COMPLETED'"
```

### 6. Workflow Versioning
Safe deployments with `Workflow.getVersion()`:
- v1: Original validation
- v2: Added fraud check before validation
- Running workflows continue with their version

### 7. Rate Limiting
Worker-level limits prevent overwhelming external services:
- Max 10 concurrent activities
- Max 20 concurrent workflow tasks

### 8. Graceful Shutdown
Sticky task queue drain for clean termination:
- Workers stop polling normal task queue
- Continue processing cached workflows
- 30-second drain timeout

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

## Testing

```bash
# Run workflow unit tests (uses TestWorkflowEnvironment)
./gradlew :temporal:test
```

Tests use Temporal's `TestWorkflowEnvironment` for:
- In-memory Temporal server
- Mocked activities for deterministic testing
- Time skipping for timer tests

## Development Notes

- Activities simulate transient failures (10-20% rate) for retry demo
- Virtual threads enabled for improved concurrency
- Temporal UI at http://localhost:8233 for workflow inspection
- Search attributes require Temporal server configuration for persistence
