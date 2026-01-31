# Modulith Project

Spring Modulith demo showcasing modular monolith architecture with event-driven communication.

## Quick Start

```bash
# Start MySQL
docker run -d --network devnet --name demodb \
  -e MYSQL_ROOT_PASSWORD=root -e MYSQL_DATABASE=demodb \
  -p 3306:3306 mysql:8.4

# Run application
./gradlew modulith:bootRun
```

- Swagger UI: http://localhost:8080/swagger-ui.html
- Actuator: http://localhost:8080/actuator/modulith

## Architecture

```
modulith/
├── customer/          # Customer registration (no dependencies)
├── order/             # Order management (depends on customer)
├── inventory/         # Stock management (listens to order events)
└── shared/            # Cross-cutting concerns (API response, exceptions)
```

### Module Dependencies

| Module    | Allowed Dependencies | Listens To              |
|-----------|---------------------|-------------------------|
| customer  | shared              | -                       |
| order     | customer, shared    | CustomerRegisteredEvent |
| inventory | shared              | OrderCreatedEvent       |

## Key Patterns

### Module Boundaries

Each module declares dependencies in `package-info.java`:

```java
@ApplicationModule(
    displayName = "Order Module",
    allowedDependencies = {"customer", "shared"}
)
package com.example.modulith.order;
```

### Facade Pattern

Modules expose public APIs via Facade classes:

```java
@Component
public class CustomerFacade {
    public boolean customerExists(Long customerId) { ... }
}
```

### Event-Driven Communication

Cross-module communication uses Spring events:

```java
// Publishing (OrderService)
eventPublisher.publishEvent(new OrderCreatedEvent(orderId, customerId, amount, createdAt));

// Listening (InventoryEventListener)
@ApplicationModuleListener
public void on(OrderCreatedEvent event) { ... }
```

## API Endpoints

| Method | Endpoint                          | Description          |
|--------|-----------------------------------|----------------------|
| POST   | /api/customers                    | Register customer    |
| GET    | /api/customers/{id}/exists        | Check customer exists|
| POST   | /api/orders                       | Create order         |
| GET    | /api/inventory/check/{sku}        | Check stock          |
| POST   | /api/inventory/reserve            | Reserve stock        |

## Database

- MySQL 8.4 with Flyway migrations
- Tables: `customers`, `orders`, `products`, `event_publication`
- Event publication table tracks async event completion

## Tech Stack

- Java 21+ with Virtual Threads
- Spring Boot 3.x + Spring Modulith
- JPA/Hibernate, Flyway, Lombok
- springdoc-openapi for Swagger

## Testing

```bash
./gradlew modulith:test
```

Spring Modulith provides module structure verification via `spring-modulith-starter-test`.

## Configuration

Key settings in `application.yml`:
- Virtual threads enabled
- JPA open-in-view disabled
- Modulith event JDBC persistence enabled
- Actuator exposes health, metrics, modulith endpoints
