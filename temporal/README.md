# Temporal Demo

## Overview

A Temporal Java demo project demonstrating workflow orchestration with:
- Saga pattern for distributed transactions
- Workflow signals and queries
- Activity heartbeat for long operations
- Search attributes for workflow discovery
- Workflow versioning for safe deployments
- Rate limiting and graceful shutdown

## Architecture Overview

```
---------------
│   Client    │ Starts Order Workflow
---------------
       │
       ├── [Signal] cancelOrder(reason)
       ├── [Signal] updateShippingAddress(address)
       ├── [Query] getStatus()
       │
       v
-----------------------------------------------------------
│              Order Processing Workflow                  │
│  [Search Attributes: CustomerId, OrderAmount, Status]   │
│                                                         │
│  1. Validate Order Activity                             │
│     └── Fraud Check (v2 versioned feature)              │
│  2. Payment Child Workflow                              │
│     ├─→ Authorize Payment Activity [heartbeat]          │
│     ├─→ Capture Payment Activity [heartbeat]            │
│     └── [Saga] Refund on failure                        │
│  3. Inventory Child Workflow                            │
│     ├─→ Check Inventory Activity                        │
│     ├─→ Reserve Inventory Activity                      │
│     └── [Saga] Release on failure                       │
│  4. Send Notification Activity                          │
-----------------------------------------------------------
       │
       v
---------------
│   Worker    │ Executes Workflows & Activities
│             │ [Rate Limited: 10 activities, 20 workflows]
---------------
```

## Quick Start

1. Start Temporal with docker

   ```bash
   docker run -d --name temporal --user root -p 7233:7233 -p 8233:8233 -v temporal-data:/data temporalio/temporal:1.5.1 server start-dev --ip 0.0.0.0 --db-filename /data/temporal.db
   ```

2. Run the Application

   ```bash
   ./gradlew :temporal:bootRun
   ```

3. API Tests

- Start a workflow (async - recommended)

   ```bash
   curl -X POST http://localhost:8080/api/orders \
     -H "Content-Type: application/json" \
     -d '{"customerId":"CUST-123","amount":9999,"description":"Premium Widget"}'
   ```

- Start a workflow (sync - for quick demo only)

   ```bash
   curl -X POST http://localhost:8080/api/orders/sync \
     -H "Content-Type: application/json" \
     -d '{"customerId":"CUST-456","amount":5000,"description":"Basic Widget"}'
   ```

- Check workflow status
   ```bash
   curl http://localhost:8080/api/orders/ORD-XXXXXXXX/status
   ```

## Testing

Run workflow unit tests:

```bash
./gradlew :temporal:test
```

Tests use `TestWorkflowEnvironment` for isolated testing with mocked activities.

## Key Features

| Feature | Description |
|---------|-------------|
| Saga Pattern | Automatic compensation on failure (refund, release inventory) |
| Signals | Cancel order, update shipping address during processing |
| Queries | Read order status and shipping address |
| Heartbeat | Long-running activities report progress |
| Search Attributes | Filter workflows by customer, amount, status |
| Versioning | Safe deployments with backward compatibility |
| Rate Limiting | Prevent overwhelming external services |
