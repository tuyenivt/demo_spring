# Versioning Subproject

## Overview
Demonstrates API versioning strategies in Spring Boot using Spring Data REST.

## Project Structure
```
versioning/
├── src/main/java/com/example/versioning/
│   ├── EmployeeApplication.java      # Main entry point
│   ├── Employee.java                 # JPA entity with status and hireDate
│   ├── EmployeeRepository.java       # Spring Data repository
│   ├── EmployeeService.java          # Service layer for version-specific DTOs
│   ├── EmployeeController.java       # URI path versioned controller
│   ├── EmployeeControllerV1.java     # V1 controller with deprecation headers
│   ├── EmployeeControllerV2.java     # V2 controller with rich DTOs
│   ├── DepartmentController.java     # Header versioned controller
│   ├── ProductController.java        # Media type versioned controller
│   ├── VersionDiscoveryController.java # API version discovery
│   ├── config/
│   │   ├── ApiVersionInterceptor.java  # Centralized version detection
│   │   └── WebConfig.java              # Web MVC configuration
│   └── dto/
│       ├── EmployeeResponseV1.java   # V1 response (simple)
│       ├── EmployeeResponseV2.java   # V2 response (rich)
│       ├── EmployeeStatus.java       # Employee status enum
│       ├── ProductV1.java            # V1 product DTO
│       └── ProductV2.java            # V2 product DTO
└── src/main/resources/
    └── application.properties        # Config with basePath=/v2
```

## Versioning Strategies

### 1. URI Path Versioning (Primary)
- Configured via `spring.data.rest.basePath=/v2`
- All Spring Data REST endpoints use `/v2` prefix
- Example: `GET /v2/employees`

### 2. Header Versioning (Secondary)
- Uses `Accept-version` custom header
- Example: `GET /location` with header `Accept-version: v2`

### 3. Media Type Versioning (Content Negotiation)
- Uses vendor-specific MIME types in Accept header
- Example: `GET /api/products` with header `Accept: application/vnd.company.v2+json`

## Key Endpoints
| Method | Path | Description |
|--------|------|-------------|
| GET | /v2/employees | List all employees via Spring Data REST (paginated) |
| POST | /v2/employees | Create employee via Spring Data REST |
| GET | /v1/employees | V1 employee list with deprecation headers |
| GET | /api/v2/employees | V2 employee list with rich DTOs |
| GET | /v2/schedule | Get schedule (v2) |
| GET | /location | Get location (requires `Accept-version: v2` header) |
| GET | /api/products | Get product (requires media type `application/vnd.company.v1+json` or `v2+json`) |
| GET | /api/versions | Discover available API versions |
| GET | /actuator/health | Health check |

## Deprecation Headers
V1 endpoints include standard deprecation headers:
- `Deprecation: true`
- `Sunset: Sat, 31 Dec 2025 23:59:59 GMT`
- `Link: </v2/employees>; rel="successor-version"`

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

## Testing
```bash
./gradlew :versioning:test
```

## Version Management Strategy
- Master branch = v2 (active development)
- v1 branch = legacy version (maintenance)
- Bug fixes cherry-picked across branches
