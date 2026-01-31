# Versioning Subproject

## Overview
Demonstrates API versioning strategies in Spring Boot using Spring Data REST.

## Project Structure
```
versioning/
├── src/main/java/com/example/versioning/
│   ├── EmployeeApplication.java    # Main entry point
│   ├── Employee.java               # JPA entity
│   ├── EmployeeRepository.java     # Spring Data repository
│   ├── EmployeeController.java     # URI path versioned controller
│   └── DepartmentController.java   # Header versioned controller
└── src/main/resources/
    └── application.properties      # Config with basePath=/v2
```

## Versioning Strategies

### 1. URI Path Versioning (Primary)
- Configured via `spring.data.rest.basePath=/v2`
- All Spring Data REST endpoints use `/v2` prefix
- Example: `GET /v2/employees`

### 2. Header Versioning (Secondary)
- Uses `Accept-version` custom header
- Example: `GET /location` with header `Accept-version: v2`

## Key Endpoints
| Method | Path | Description |
|--------|------|-------------|
| GET | /v2/employees | List all employees (paginated) |
| POST | /v2/employees | Create employee |
| GET | /v2/schedule | Get schedule (v2) |
| GET | /location | Get location (requires `Accept-version: v2` header) |
| GET | /actuator/health | Health check |

## Tech Stack
- Spring Boot with Spring Data REST
- Spring Data JPA
- H2 in-memory database
- Bean Validation
- Lombok

## Running
```bash
./gradlew :versioning:bootRun
```

## Version Management Strategy
- Master branch = v2 (active development)
- v1 branch = legacy version (maintenance)
- Bug fixes cherry-picked across branches
