# Swagger Subproject

## Overview

This is a **Feign-based API client** project that demonstrates consuming external REST APIs using OpenAPI code generation. It generates Java client code from an OpenAPI 3.0.3 specification, configures Feign clients with Spring Boot, and exposes a local REST API facade with Swagger UI documentation.

## Technology Stack

- **Java**: 25
- **Spring Boot**: 3.5.10
- **Spring Cloud**: 2025.0.1
- **OpenFeign**: Spring Cloud Starter + feign-okhttp + feign-jackson
- **OpenAPI Generator**: `org.openapi.generator` plugin v7.19.0
- **OpenAPI Docs**: springdoc-openapi-starter-webmvc-ui 2.8.x
- **HTTP Client**: OkHttp (via Feign)
- **Build Tool**: Gradle

## Project Structure

```
swagger/
├── build.gradle                    # Build config with OpenAPI code generation
├── openapi/
│   ├── petstore.yaml               # OpenAPI 3.0.3 specification
│   └── config-petstore.json        # Code generation settings (feign library)
└── src/main/java/com/example/openapi/
    ├── MainApplication.java        # Spring Boot entry point
    ├── config/
    │   ├── FeignConfig.java        # OkHttpClient bean for Feign
    │   ├── OpenApiConfig.java      # Springdoc OpenAPI metadata (title, server)
    │   └── PetStoreConfig.java     # Feign client beans (PetApi, StoreApi, UserApi)
    ├── controller/
    │   └── PetController.java      # REST facade: GET /api/pets, GET /api/pets/{id}, POST /api/pets
    ├── dto/
    │   ├── CreatePetRequest.java   # Validated request record
    │   ├── PetResponse.java        # Response record
    │   └── ErrorResponse.java      # Standard error record (timestamp, status, message)
    └── exception/
        └── GlobalExceptionHandler.java  # Handles FeignException, validation, generic errors
```

## Key Components

### Configuration Classes

- `FeignConfig.java`: Provides `feign.Client` bean backed by OkHttpClient
- `OpenApiConfig.java`: Configures Springdoc `OpenAPI` bean (title, version, local server)
- `PetStoreConfig.java`: `@ConfigurationProperties(prefix = "app.pet-store")` — builds PetApi, StoreApi, UserApi Feign clients with Basic Auth

### REST Facade (PetController)

| Method | Path                | Description                                                                       |
|--------|---------------------|-----------------------------------------------------------------------------------|
| GET    | `/api/pets/{petId}` | Get pet by ID (`@Positive` validated)                                             |
| GET    | `/api/pets?status=` | Find pets by status (default: `available`; validated: `available\|pending\|sold`) |
| POST   | `/api/pets`         | Create a pet (`@Valid` body)                                                      |

### Generated Code (`build/openapi/src/main/java/`)

- **API Clients**: `com.example.openapi.petstore.api.{PetApi, StoreApi, UserApi}`
- **Models**: `com.example.openapi.petstore.model.{Pet, Category, Tag, Order, User, ApiResponse}`
- **Infrastructure**: `com.example.openapi.petstore.invoker.*`

### Exception Handling

`GlobalExceptionHandler` maps:
- `MethodArgumentNotValidException` → 400
- `ConstraintViolationException` → 400
- `FeignException.NotFound` → 404
- `FeignException` → 502
- `Exception` → 500

## Build Commands

```bash
# Build and generate OpenAPI client code
./gradlew :swagger:build

# Run the application
./gradlew :swagger:bootRun

# Run tests
./gradlew :swagger:test
```

## Configuration

Key properties in `application.yml` (override via env vars):

```yaml
app:
  pet-store:
    base-url: ${PET_STORE_BASE_URL:https://petstore.swagger.io/v2}
    user-name: ${PET_STORE_USERNAME:user}
    password: ${PET_STORE_PASSWORD:pass}
```

## Code Generation

OpenAPI client is generated at build time via `openApiGenerate` task:
- Input spec: `openapi/petstore.yaml`
- Config: `openapi/config-petstore.json`
- Library: `feign`, Jakarta EE, `java8-localdatetime`, no nullable wrappers
- Output: `build/openapi/src/main/java/` (added to `sourceSets.main.java`)

## API Docs (Springdoc)

When running locally:
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

## Notes

- Base package changed from `com.example.swagger` → `com.example.openapi`
- Spec directory renamed from `swagger/` → `openapi/`
- Generated packages: `com.example.openapi.petstore.{api,model,invoker}`
- The project now **exposes** its own REST endpoints in addition to consuming Petstore
- Authentication to Petstore uses Basic Auth via `BasicAuthRequestInterceptor`
