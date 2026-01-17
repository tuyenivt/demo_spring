# temporal

## Overview

- A temporal Java demo project.

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
