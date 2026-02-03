# Database Replication Project

## Project Overview

Spring Boot application demonstrating **MySQL master-slave replication** with read-write separation using GTID mode.

## Quick Reference

### Key Paths

- **Main Application**: `src/main/java/com/example/database/replication/MainApplication.java`
- **Writer Config**: `src/main/java/.../config/WriterDatabaseConfig.java` (master, port 3306)
- **Reader Config**: `src/main/java/.../config/ReaderDatabaseConfig.java` (replica, port 3307)
- **Write Repository**: `src/main/java/.../repository/write/UserWriteRepository.java`
- **Read Repository**: `src/main/java/.../repository/read/UserReadRepository.java`
- **Docker Setup**: `docker/docker-compose.yml`
- **Schema Migrations**: `src/main/resources/db/changelog/db.changelog-master.xml`

### Architecture Pattern

```
[Client] --> [Controller] --> [Service] --> [Write Repo] --> [Master DB :3306]
                                        --> [Read Repo]  --> [Replica DB :3307]
```

### Transaction Managers

- `writerTransactionManager` - for write operations (master)
- `readerTransactionManager` - for read operations (replica)

### Important Configurations

- Liquibase runs **only on master** (writer datasource)
- Reader datasource has `hibernate.connection.readOnly=true`
- GTID replication ensures consistency across failovers

## Common Commands

```bash
# Start databases
cd docker && docker compose up -d

# Stop and clean
cd docker && docker compose down --volumes --remove-orphans

# Run application
./gradlew :database-replication:bootRun

# Run tests
./gradlew :database-replication:test
```

## API Endpoints

| Method | Path                 | Description  | DB Target      |
|--------|----------------------|--------------|----------------|
| POST   | `/users`             | Create user  | Master (write) |
| GET    | `/users/{id}`        | Get by ID    | Replica (read) |
| GET    | `/users/name/{name}` | Find by name | Replica (read) |
| DELETE | `/users/{id}`        | Delete user  | Master (write) |

## Key Implementation Details

1. **Read-After-Write Consistency**: `createUser()` reads from master immediately after insert
2. **Separate EntityManagerFactory**: Each datasource has its own persistence unit
3. **Repository Package Segregation**: Write repos in `.repository.write`, read repos in `.repository.read`
4. **GTID Mode**: Uses `SOURCE_AUTO_POSITION=1` for automatic position tracking

## Gotchas

- Replica is read-only (`--read-only=1`) - writes will fail
- Replication lag may cause stale reads from replica
- Both databases must have identical schemas (Liquibase runs on master, replicates to slave)
