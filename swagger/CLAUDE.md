# Swagger Subproject

## Overview

This is a **Feign-based API client** project that demonstrates consuming external REST APIs using Swagger/OpenAPI code generation. It generates Java client code from an OpenAPI 2.0 specification and configures Feign clients with Spring Boot.

## Technology Stack

- **Java**: 21
- **Spring Boot**: 3.5.9
- **Spring Cloud**: 2025.0.1
- **OpenFeign**: Spring Cloud Starter
- **Swagger**: OpenAPI 2.0 spec with swagger-codegen 3.0.75
- **HTTP Client**: OkHttp (via Feign)
- **Build Tool**: Gradle with swagger-codegen plugin

## Project Structure

```
swagger/
├── build.gradle                    # Build config with swagger code generation
├── src/main/java/com/example/swagger/
│   ├── MainApplication.java        # Spring Boot entry point
│   └── config/
│       ├── FeignConfig.java        # OkHttp client configuration
│       └── PetStoreConfig.java     # Feign client beans (PetApi, StoreApi, UserApi)
├── src/main/resources/
│   └── application.yml             # App configuration with Petstore URL/credentials
└── swagger/
    ├── petstore.yaml               # OpenAPI 2.0 specification
    └── config-petstore.json        # Code generation settings
```

## Key Components

### Configuration Classes

- `FeignConfig.java`: Provides OkHttpClient bean for Feign
- `PetStoreConfig.java`: Wires up PetApi, StoreApi, UserApi beans with authentication

### Generated Code (build/swagger-code-petstore/)

- **API Clients**: PetApi, StoreApi, UserApi
- **Models**: Pet, Category, Tag, Order, User, ModelApiResponse
- **Infrastructure**: ApiClient, auth handlers, utilities

## API Endpoints (Petstore)

| API | Endpoints |
|-----|-----------|
| PetApi | CRUD operations for pets, image upload |
| StoreApi | Inventory, order management |
| UserApi | User CRUD, login/logout |

## Build Commands

```bash
# Build and generate Swagger code
./gradlew :swagger:build

# Run the application
./gradlew :swagger:bootRun
```

## Configuration

Key properties in `application.yml`:
```yaml
app:
  pet-store:
    base-url: https://petstore.swagger.io/v2
    user-name: user
    password: pass
```

## Code Generation

Swagger code is generated at build time:
- Input: `swagger/petstore.yaml`
- Config: `swagger/config-petstore.json`
- Output: `build/swagger-code-petstore/src/main/java/`

## Notes

- This project consumes external APIs, it does not expose its own REST endpoints
- Authentication uses Basic Auth via FeignInterceptor
- Generated packages: `io.swagger.petstore.{api,model,invoker}`
