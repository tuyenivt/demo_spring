# temporal

## Overview

- A temporal Java demo project with graceful shutdown support.

## Architecture Overview

```
---------------
│   Client    │ Starts Order Workflow
---------------
       │
       v
-----------------------------------------------------------
│              Order Processing Workflow                  │
│  1. Validate Order Activity                             │
│  2. Payment Child Workflow                              │
│     |=> Authorize Payment Activity                      │
│     |=> Capture Payment Activity                        │
│  3. Inventory Child Workflow                            │
│     |=> Check Inventory Activity                        │
│     |=> Reserve Inventory Activity                      │
│  4. Send Notification Activity                          │
-----------------------------------------------------------
       │
       v
---------------
│   Worker    | Executes Workflows & Activities
---------------
```

## Quick Start

1. Start Temporal with docker

    ```shell
    docker network create devnet
    docker run -d --network devnet --name temporal --user root -p 7233:7233 -p 8233:8233 -v temporal-data:/data temporalio/temporal:1.5.1 server start-dev --ip 0.0.0.0 --db-filename /data/temporal.db
    ```

2. Run the Application

    ```shell
    ./gradlew clean bootRun
    ```

3. API Tests

    - Start a workflow manually (async - recommended)
    
    ```shell
    curl -X POST http://localhost:8080/api/orders -H "Content-Type: application/json" -d '{"customerId":"CUST-123","amount":9999,"description":"Premium Widget"}'
    ```
    
    - Start a workflow (sync - for quick demo only)
    
    ```shell
    curl -X POST http://localhost:8080/api/orders/sync -H "Content-Type: application/json" -d '{"customerId":"CUST-456","amount":5000,"description":"Basic Widget"}'
    ```
    
    - Check workflow status
    ```shell
    curl http://localhost:8080/api/orders/ORD-XXXXXXXX/status
    ```
