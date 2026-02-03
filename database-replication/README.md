# Database Replication Demo

A Spring Boot application demonstrating **MySQL master-slave replication** with read-write separation using GTID (Global Transaction Identifier) mode.

## Overview

This project showcases how to implement database replication in a Spring application:

- **Write operations** route to the master database
- **Read operations** route to the replica database
- **Read-after-write consistency** ensures fresh data after inserts

## Architecture

```
                                  ┌─────────────────┐
                                  │  Master (Write) │
                           ┌─────►│   MySQL :3306   │
                           │      └────────┬────────┘
┌────────┐    ┌─────────┐  │               │ Replication
│ Client │───►│ Service │──┤               ▼
└────────┘    └─────────┘  │      ┌─────────────────┐
                           │      │ Replica (Read)  │
                           └─────►│   MySQL :3307   │
                                  └─────────────────┘
```

## Tech Stack

- Java 21+
- Spring Boot 3.x
- Spring Data JPA
- MySQL 8.4 (GTID replication)
- Liquibase (schema migrations)
- Docker Compose

## Quick Start

### 1. Start the Databases

```bash
cd docker
docker compose up -d
```

This starts:
- **mysql-writer** (master) on port 3306
- **mysql-reader** (replica) on port 3307

### 2. Run the Application

```bash
./gradlew :database-replication:bootRun
```

### 3. Test the API

```bash
# Create a user (writes to master)
curl -X POST http://localhost:8080/users \
  -H "Content-Type: application/json" \
  -d '{"name": "John Doe", "email": "john@example.com"}'

# Get user by ID (reads from replica)
curl http://localhost:8080/users/1

# Find users by name (reads from replica)
curl "http://localhost:8080/users/name/John%20Doe"

# Delete a user (writes to master)
curl -X DELETE http://localhost:8080/users/1
```

### 4. Clean Up

```bash
cd docker
docker compose down --volumes --remove-orphans
```

## Key Concepts

### Read-Write Separation

- **WriterDatabaseConfig**: Configures the master datasource with its own `EntityManagerFactory` and `TransactionManager`
- **ReaderDatabaseConfig**: Configures the replica datasource as read-only

### GTID Replication

GTID (Global Transaction Identifier) provides:
- Automatic failover positioning
- Consistent replication across all servers
- No need to track binary log positions manually

### Read-After-Write Consistency

When creating a user, the service reads immediately from the master to ensure the response contains the newly created data (avoids replication lag issues).

## API Reference

| Method | Endpoint             | Description        | Database |
|--------|----------------------|--------------------|----------|
| POST   | `/users`             | Create a new user  | Master   |
| GET    | `/users/{id}`        | Get user by ID     | Replica  |
| GET    | `/users/name/{name}` | Find users by name | Replica  |
| DELETE | `/users/{id}`        | Delete a user      | Master   |

## Troubleshooting

### Check Replication Status

```bash
docker exec -it mysql-reader mysql -uroot -proot -e "SHOW REPLICA STATUS\G"
```

### View Logs

```bash
docker logs mysql-writer
docker logs mysql-reader
```

### Common Issues

1. **Replica not syncing**: Ensure master is fully started before replica
2. **Connection refused**: Wait for MySQL containers to be healthy
3. **Stale reads**: Expected due to replication lag; use read-after-write pattern for critical reads
