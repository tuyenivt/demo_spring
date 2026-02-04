# Database Replication Project

## Project Overview

Spring Boot application demonstrating **MySQL master-slave replication** with read-write separation using GTID mode and **AbstractRoutingDataSource** with annotation-based routing.

## Quick Reference

### Key Paths

- **Main Application**: `src/main/java/com/example/database/replication/MainApplication.java`
- **DataSource Config**: `src/main/java/.../config/DataSourceConfig.java` (routing datasource)
- **Repository**: `src/main/java/.../repository/UserRepository.java`
- **Routing**:
  - `src/main/java/.../routing/UseWriter.java` - annotation for write methods
  - `src/main/java/.../routing/DataSourceContextHolder.java` - ScopedValue-based context
  - `src/main/java/.../routing/RoutingDataSource.java` - AbstractRoutingDataSource impl
  - `src/main/java/.../routing/UseWriterAspect.java` - aspect for @UseWriter
- **DTOs**: `src/main/java/.../dto/CreateUserRequest.java`, `UserResponse.java`
- **Aspect**: `src/main/java/.../aspect/DatabaseRoutingAspect.java` (logging)
- **Docker Setup**: `docker/docker-compose.yml`
- **Schema Migrations**: `src/main/resources/db/changelog/db.changelog-master.xml`

### Architecture Pattern

```
[Client] --> [Controller] --> [Service] --> [UserRepository] --> [RoutingDataSource]
                |                 |                                      |
                |             @UseWriter?                        +-------+-------+
                |                 |                              |               |
             [DTOs]          Yes: WRITER                     [Writer DS]   [Reader DS]
                             No:  READER                     (port 3306)   (port 3307)
```

### Routing Mechanism

- **Default**: All operations route to READER datasource
- **@UseWriter**: Methods annotated with `@UseWriter` route to WRITER datasource
- **Context**: Uses `ScopedValue` for thread-safe routing context (compatible with virtual threads)
- **Aspect**: `UseWriterAspect` intercepts annotated methods and sets the routing context

### Important Configurations

- Single unified `UserRepository` (no separate read/write repositories)
- `RoutingDataSource` extends `AbstractRoutingDataSource` for dynamic routing
- Liquibase runs **only on master** (writer datasource)
- GTID replication ensures consistency across failovers
- Input validation with `@Valid` and Bean Validation annotations
- OpenAPI documentation available at `/swagger-ui.html`

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

| Method | Path                 | Description           | DB Target      |
|--------|----------------------|-----------------------|----------------|
| POST   | `/users`             | Create user           | Master (write) |
| GET    | `/users`             | List users (paginated)| Replica (read) |
| GET    | `/users/{id}`        | Get by ID             | Replica (read) |
| GET    | `/users/name/{name}` | Find by name          | Replica (read) |
| DELETE | `/users/{id}`        | Delete user           | Master (write) |

### Pagination Parameters

- `page` - Page number (default: 0)
- `size` - Page size (default: 20)

Example: `GET /users?page=0&size=10`

## Key Implementation Details

1. **@UseWriter Annotation**: Mark service methods that need writer datasource
2. **ScopedValue Context**: Thread-safe, virtual-thread compatible routing context
3. **AbstractRoutingDataSource**: Dynamic datasource selection at runtime
4. **Unified Repository**: Single `UserRepository` for all operations
5. **Read-After-Write Consistency**: `findByIdAfterWrite()` uses `@UseWriter` for immediate consistency
6. **GTID Mode**: Uses `SOURCE_AUTO_POSITION=1` for automatic position tracking
7. **DTOs**: Request/Response DTOs decouple API contracts from entities
8. **Input Validation**: Bean Validation on `CreateUserRequest` (name required, valid email)
9. **DatabaseRoutingAspect**: Logs routing decisions for debugging (DEBUG level)
10. **OpenAPI/Swagger**: Auto-generated API docs at `/swagger-ui.html`

## Usage Example

```java
@Service
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;

    // Write operation - routes to master
    @UseWriter
    @Transactional
    public User createUser(CreateUserRequest request) {
        return userRepository.save(user);
    }

    // Read operation - routes to replica (default)
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }
}
```

## Gotchas

- Replica is read-only (`--read-only=1`) - writes will fail if routing is misconfigured
- Replication lag may cause stale reads from replica
- Both databases must have identical schemas (Liquibase runs on master, replicates to slave)
- `@UseWriter` must be on the service method, not the repository method
- Aspect order matters: `UseWriterAspect` runs before `@Transactional`
